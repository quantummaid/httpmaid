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
import de.quantummaid.eventmaid.messageBus.MessageBus;
import de.quantummaid.eventmaid.processingContext.EventType;
import de.quantummaid.eventmaid.serializedMessageBus.SerializedMessageBus;
import de.quantummaid.eventmaid.useCases.useCaseAdapter.LowLevelUseCaseAdapterBuilder;
import de.quantummaid.eventmaid.useCases.useCaseAdapter.UseCaseAdapter;
import de.quantummaid.httpmaid.chains.*;
import de.quantummaid.httpmaid.events.EventFactory;
import de.quantummaid.httpmaid.events.EventModule;
import de.quantummaid.httpmaid.handler.distribution.DistributableHandler;
import de.quantummaid.httpmaid.handler.distribution.HandlerDistributors;
import de.quantummaid.httpmaid.startupchecks.StartupChecks;
import de.quantummaid.httpmaid.usecases.instantiation.UseCaseInstantiator;
import de.quantummaid.httpmaid.usecases.instantiation.UseCaseInstantiatorFactory;
import de.quantummaid.httpmaid.usecases.method.UseCaseMethod;
import de.quantummaid.httpmaid.usecases.serializing.SerializationAndDeserializationProvider;
import de.quantummaid.httpmaid.usecases.serializing.UseCaseSerializationAndDeserialization;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.*;

import static de.quantummaid.eventmaid.internal.collections.filtermap.FilterMapBuilder.filterMapBuilder;
import static de.quantummaid.eventmaid.internal.collections.predicatemap.PredicateMapBuilder.predicateMapBuilder;
import static de.quantummaid.eventmaid.mapping.ExceptionMapifier.defaultExceptionMapifier;
import static de.quantummaid.eventmaid.processingContext.EventType.eventTypeFromClass;
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
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class UseCasesModule implements ChainModule {
    public static final MetaDataKey<SerializedMessageBus> SERIALIZED_MESSAGE_BUS = metaDataKey("SERIALIZED_MESSAGE_BUS");

    private SerializationAndDeserializationProvider serializationAndDeserializationProvider;
    private UseCaseInstantiatorFactory useCaseInstantiatorFactory = types -> zeroArgumentsConstructorUseCaseInstantiator();
    private final Map<Class<?>, EventType> useCaseToEventMappings = new HashMap<>();
    private final List<UseCaseMethod> useCaseMethods = new ArrayList<>();

    public static UseCasesModule useCasesModule() {
        return new UseCasesModule();
    }

    public void setUseCaseInstantiatorFactory(final UseCaseInstantiatorFactory useCaseInstantiatorFactory) {
        this.useCaseInstantiatorFactory = useCaseInstantiatorFactory;
    }

    public void addUseCaseToEventMapping(final Class<?> useCaseClass,
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
        handlerDistributors.register(handler -> handler.handler() instanceof Class, handler -> {
            final Class<?> useCaseClass = (Class<?>) handler.handler();
            final EventType eventType = eventTypeFromClass(useCaseClass);
            useCaseToEventMappings.put(useCaseClass, eventType);
            useCaseMethods.add(useCaseMethodOf(useCaseClass));
            final DistributableHandler eventHandler = distributableHandler(handler.condition(), eventType, handler.perRouteConfigurators());
            return singletonList(eventHandler);
        });
    }

    @Override
    public void configure(final DependencyRegistry dependencyRegistry) {
        final EventModule eventModule = dependencyRegistry.getDependency(EventModule.class);
        useCaseMethods.forEach(useCaseMethod -> {
            final Class<?> useCaseClass = useCaseMethod.useCaseClass();
            final EventType eventType = useCaseToEventMappings.get(useCaseClass);
            final EventFactory eventFactory = buildEventFactory(useCaseMethod);
            eventModule.setEventFactoryFor(eventType, eventFactory);
        });
    }

    @Override
    public void register(final ChainExtender extender) {
        final LowLevelUseCaseAdapterBuilder lowLevelUseCaseAdapterBuilder = LowLevelUseCaseAdapterBuilder.aLowLevelUseCaseInvocationBuilder();
        final UseCaseSerializationAndDeserialization serializationAndDeserialization = serializationAndDeserializationProvider.provide(useCaseMethods);

        final List<Class<?>> useCaseClasses = useCaseMethods.stream()
                .map(UseCaseMethod::useCaseClass)
                .collect(toList());
        final UseCaseInstantiator useCaseInstantiator = useCaseInstantiatorFactory.createInstantiator(useCaseClasses);

        final StartupChecks startupChecks = extender.getMetaDatum(STARTUP_CHECKS);
        useCaseMethods.forEach(useCaseMethod -> {
            final Class<?> useCaseClass = useCaseMethod.useCaseClass();
            final EventType eventType = useCaseToEventMappings.get(useCaseClass);

            lowLevelUseCaseAdapterBuilder.addUseCase(useCaseClass, eventType, (useCase, event, callingContext) -> {
                final Map<String, Object> parameters = serializationAndDeserialization.deserializeParameters(event, useCaseClass);
                final Optional<Object> returnValue = useCaseMethod.invoke(useCase, parameters, event);
                return returnValue
                        .map(serializationAndDeserialization::serializeReturnValue)
                        .orElse(null);
            });
            startupChecks.addStartupCheck(() -> useCaseInstantiator.check(useCaseClass));
        });

        lowLevelUseCaseAdapterBuilder.setUseCaseInstantiator(useCaseInstantiator::instantiate);

        lowLevelUseCaseAdapterBuilder.setRequestSerializers(failingPredicateMap());
        lowLevelUseCaseAdapterBuilder.setRequestDeserializers(failingFilterMap());
        lowLevelUseCaseAdapterBuilder.setReseponseSerializers(failingPredicateMap());
        lowLevelUseCaseAdapterBuilder.setResponseDeserializers(failingFilterMap());
        final PredicateMapBuilder<Exception, Mapifier<Exception>> exceptionSerializers = predicateMapBuilder();
        exceptionSerializers.setDefaultValue(defaultExceptionMapifier());
        lowLevelUseCaseAdapterBuilder.setExceptionSerializers(exceptionSerializers);

        final UseCaseAdapter useCaseAdapter = lowLevelUseCaseAdapterBuilder.build();
        final MessageBus messageBus = extender.getMetaDatum(MESSAGE_BUS);

        final SerializedMessageBus serializedMessageBus = useCaseAdapter.attachAndEnhance(messageBus);
        extender.addMetaDatum(SERIALIZED_MESSAGE_BUS, serializedMessageBus);
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
