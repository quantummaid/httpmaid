/*
 * Copyright (c) 2020 Richard Hauswald - https://quantummaid.de/.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package de.quantummaid.httpmaid.usecases;

import de.quantummaid.eventmaid.internal.collections.filtermap.FilterMapBuilder;
import de.quantummaid.eventmaid.internal.collections.predicatemap.PredicateMapBuilder;
import de.quantummaid.eventmaid.mapping.Demapifier;
import de.quantummaid.eventmaid.mapping.Mapifier;
import de.quantummaid.eventmaid.messagebus.MessageBus;
import de.quantummaid.eventmaid.processingcontext.EventType;
import de.quantummaid.eventmaid.serializedmessagebus.SerializedMessageBus;
import de.quantummaid.eventmaid.usecases.usecaseadapter.LowLevelUseCaseAdapterBuilder;
import de.quantummaid.eventmaid.usecases.usecaseadapter.UseCaseAdapter;
import de.quantummaid.httpmaid.PerRouteConfigurator;
import de.quantummaid.httpmaid.chains.*;
import de.quantummaid.httpmaid.events.Event;
import de.quantummaid.httpmaid.events.EventFactory;
import de.quantummaid.httpmaid.events.EventModule;
import de.quantummaid.httpmaid.generator.GenerationCondition;
import de.quantummaid.httpmaid.handler.distribution.DistributableHandler;
import de.quantummaid.httpmaid.handler.distribution.HandlerDistributors;
import de.quantummaid.httpmaid.startupchecks.StartupChecks;
import de.quantummaid.httpmaid.usecases.instantiation.UseCaseInstantiator;
import de.quantummaid.httpmaid.usecases.instantiation.UseCaseInstantiatorFactory;
import de.quantummaid.httpmaid.usecases.method.UseCaseMethod;
import de.quantummaid.httpmaid.usecases.serializing.SerializationAndDeserializationProvider;
import de.quantummaid.httpmaid.usecases.serializing.UseCaseSerializationAndDeserialization;
import de.quantummaid.httpmaid.websockets.broadcast.Broadcasters;
import de.quantummaid.reflectmaid.GenericType;
import de.quantummaid.reflectmaid.ResolvedType;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.*;

import static de.quantummaid.eventmaid.internal.collections.filtermap.FilterMapBuilder.filterMapBuilder;
import static de.quantummaid.eventmaid.internal.collections.predicatemap.PredicateMapBuilder.predicateMapBuilder;
import static de.quantummaid.eventmaid.mapping.ExceptionMapifier.defaultExceptionMapifier;
import static de.quantummaid.eventmaid.processingcontext.EventType.eventTypeFromString;
import static de.quantummaid.eventmaid.usecases.usecaseadapter.LowLevelUseCaseAdapterBuilder.aLowLevelUseCaseInvocationBuilder;
import static de.quantummaid.httpmaid.chains.MetaDataKey.metaDataKey;
import static de.quantummaid.httpmaid.events.EventModule.MESSAGE_BUS;
import static de.quantummaid.httpmaid.events.EventModule.eventModule;
import static de.quantummaid.httpmaid.handler.distribution.DistributableHandler.distributableHandler;
import static de.quantummaid.httpmaid.handler.distribution.HandlerDistributors.HANDLER_DISTRIBUTORS;
import static de.quantummaid.httpmaid.startupchecks.StartupChecks.STARTUP_CHECKS;
import static de.quantummaid.httpmaid.usecases.eventfactories.MultipleParametersEventFactory.multipleParametersEventFactory;
import static de.quantummaid.httpmaid.usecases.eventfactories.SingleParameterEventFactory.singleParameterEventFactory;
import static de.quantummaid.httpmaid.usecases.instantiation.ZeroArgumentsConstructorUseCaseInstantiator.zeroArgumentsConstructorUseCaseInstantiator;
import static de.quantummaid.httpmaid.usecases.method.UseCaseMethod.useCaseMethodOf;
import static de.quantummaid.httpmaid.util.Validators.validateNotNull;
import static de.quantummaid.httpmaid.websockets.broadcast.Broadcasters.BROADCASTERS;
import static de.quantummaid.reflectmaid.GenericType.fromResolvedType;
import static de.quantummaid.reflectmaid.GenericType.genericType;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@SuppressWarnings("java:S1905")
public final class UseCasesModule implements ChainModule {
    public static final MetaDataKey<SerializedMessageBus> SERIALIZED_MESSAGE_BUS = metaDataKey("SERIALIZED_MESSAGE_BUS");

    private SerializationAndDeserializationProvider serializationAndDeserializationProvider;
    private UseCaseInstantiatorFactory useCaseInstantiatorFactory = types -> zeroArgumentsConstructorUseCaseInstantiator();
    private final Map<ResolvedType, EventType> useCaseToEventMappings = new HashMap<>();
    private final List<UseCaseMethod> useCaseMethods = new ArrayList<>();

    public static UseCasesModule useCasesModule() {
        return new UseCasesModule();
    }

    public void setUseCaseInstantiatorFactory(final UseCaseInstantiatorFactory useCaseInstantiatorFactory) {
        this.useCaseInstantiatorFactory = useCaseInstantiatorFactory;
    }

    public void addUseCaseToEventMapping(final ResolvedType useCaseClass,
                                         final EventType eventType) {
        validateNotNull(useCaseClass, "useCaseClass");
        validateNotNull(eventType, "eventType");
        useCaseToEventMappings.put(useCaseClass, eventType);
    }

    public void setSerializationAndDeserializationProvider(final SerializationAndDeserializationProvider serializationAndDeserializationProvider) {
        this.serializationAndDeserializationProvider = serializationAndDeserializationProvider;
    }

    @Override
    public List<ChainModule> supplyModulesIfNotAlreadyPresent() {
        return singletonList(eventModule());
    }

    @Override
    public void init(final MetaData configurationMetaData) {
        final HandlerDistributors handlerDistributors = configurationMetaData.get(HANDLER_DISTRIBUTORS);
        handlerDistributors.register(handler -> handler.handler() instanceof GenericType, handler -> {
            final GenericType<?> useCaseClass = (GenericType<?>) handler.handler();
            return registerUseCase(useCaseClass, handler.condition(), handler.perRouteConfigurators());
        });
        handlerDistributors.register(handler -> handler.handler() instanceof Class, handler -> {
            final Class<?> clazz = (Class<?>) handler.handler();
            final GenericType<?> useCaseClass = genericType(clazz);
            return registerUseCase(useCaseClass, handler.condition(), handler.perRouteConfigurators());
        });
    }

    private List<DistributableHandler> registerUseCase(final GenericType<?> genericType,
                                                       final GenerationCondition condition,
                                                       final List<PerRouteConfigurator> perRouteConfigurators) {
        final ResolvedType resolvedType = genericType.toResolvedType();
        final EventType eventType = eventTypeFromString(resolvedType.description());
        useCaseToEventMappings.put(resolvedType, eventType);
        useCaseMethods.add(useCaseMethodOf(resolvedType));
        final DistributableHandler eventHandler = distributableHandler(condition, eventType, perRouteConfigurators);
        return singletonList(eventHandler);
    }

    @Override
    public void configure(final DependencyRegistry dependencyRegistry) {
        final EventModule eventModule = dependencyRegistry.getDependency(EventModule.class);
        useCaseMethods.forEach(useCaseMethod -> {
            final ResolvedType useCaseClass = useCaseMethod.useCaseClass();
            final EventType eventType = useCaseToEventMappings.get(useCaseClass);
            final EventFactory eventFactory = buildEventFactory(useCaseMethod);
            eventModule.setEventFactoryFor(eventType, eventFactory);
        });
    }

    @Override
    public void register(final ChainExtender extender) {
        final Broadcasters broadcasters = extender.getMetaDatum(BROADCASTERS);
        final Collection<Class<?>> injectionTypes = broadcasters.injectionTypes();
        final LowLevelUseCaseAdapterBuilder adapterBuilder = createAdapterBuilder();
        final UseCaseSerializationAndDeserialization serializationAndDeserialization = serializationAndDeserializationProvider.provide(useCaseMethods, injectionTypes);

        final List<Class<?>> useCaseClasses = useCaseMethods.stream()
                .map(UseCaseMethod::useCaseClass)
                .map(ResolvedType::assignableType)
                .collect(toList());
        final UseCaseInstantiator useCaseInstantiator = useCaseInstantiatorFactory.createInstantiator(useCaseClasses);

        final StartupChecks startupChecks = extender.getMetaDatum(STARTUP_CHECKS);
        useCaseMethods.forEach(useCaseMethod -> {
            final ResolvedType useCaseClass = useCaseMethod.useCaseClass();
            final EventType eventType = useCaseToEventMappings.get(useCaseClass);
            adapterBuilder.addUseCase(useCaseClass.assignableType(), eventType, (useCase, untypedEvent, callingContext) -> {
                final Event event = (Event) untypedEvent;
                final Map<String, Object> parameters = serializationAndDeserialization.deserializeParameters(event, useCaseClass);
                final Optional<Object> returnValue = useCaseMethod.invoke(useCase, parameters, event);
                return returnValue
                        .map(object -> serializationAndDeserialization.serializeReturnValue(object, useCaseMethod.returnType().orElseThrow()))
                        .orElse(null);
            });
            startupChecks.addStartupCheck(() -> useCaseInstantiator.check(fromResolvedType(useCaseClass)));
        });

        adapterBuilder.setUseCaseInstantiator(useCaseInstantiator::instantiate);

        final PredicateMapBuilder<Exception, Mapifier<Exception>> exceptionSerializers = predicateMapBuilder();
        exceptionSerializers.setDefaultValue(defaultExceptionMapifier());
        adapterBuilder.setExceptionSerializers(exceptionSerializers);

        final UseCaseAdapter useCaseAdapter = adapterBuilder.build();
        final MessageBus messageBus = extender.getMetaDatum(MESSAGE_BUS);

        final SerializedMessageBus serializedMessageBus = useCaseAdapter.attachAndEnhance(messageBus);
        extender.addMetaDatum(SERIALIZED_MESSAGE_BUS, serializedMessageBus);
    }

    private static LowLevelUseCaseAdapterBuilder createAdapterBuilder() {
        final LowLevelUseCaseAdapterBuilder adapterBuilder = aLowLevelUseCaseInvocationBuilder();
        adapterBuilder.setRequestSerializers(failingPredicateMap());
        adapterBuilder.setRequestDeserializers(failingFilterMap());
        adapterBuilder.setReseponseSerializers(failingPredicateMap());
        adapterBuilder.setResponseDeserializers(failingFilterMap());
        return adapterBuilder;
    }

    private static FilterMapBuilder<Class<?>, Object, Demapifier<?>> failingFilterMap() {
        final FilterMapBuilder<Class<?>, Object, Demapifier<?>> filterMap = filterMapBuilder();
        filterMap.setDefaultValue((targetType, map) -> {
            throw new UnsupportedOperationException();
        });
        return filterMap;
    }

    private static PredicateMapBuilder<Object, Mapifier<Object>> failingPredicateMap() {
        final PredicateMapBuilder<Object, Mapifier<Object>> predicateMap = predicateMapBuilder();
        predicateMap.setDefaultValue(object -> {
            throw new UnsupportedOperationException();
        });
        return predicateMap;
    }

    private static EventFactory buildEventFactory(final UseCaseMethod useCaseMethod) {
        if (useCaseMethod.isSingleParameterUseCase()) {
            final String name = useCaseMethod.singleParameterName();
            return singleParameterEventFactory(name);
        } else {
            final List<String> parameterNames = useCaseMethod.parameterNames();
            return multipleParametersEventFactory(parameterNames);
        }
    }
}
