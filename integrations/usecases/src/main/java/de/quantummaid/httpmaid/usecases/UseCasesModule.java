/*
 * Copyright (c) 2019 Richard Hauswald - https://quantummaid.de/.
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

import de.quantummaid.httpmaid.usecases.serializing.SerializerAndDeserializer;
import de.quantummaid.httpmaid.chains.ChainExtender;
import de.quantummaid.httpmaid.chains.ChainModule;
import de.quantummaid.httpmaid.chains.MetaData;
import de.quantummaid.httpmaid.chains.MetaDataKey;
import de.quantummaid.httpmaid.handler.distribution.HandlerDistributors;
import de.quantummaid.messagemaid.internal.collections.filtermap.FilterMapBuilder;
import de.quantummaid.messagemaid.internal.collections.predicatemap.PredicateMapBuilder;
import de.quantummaid.messagemaid.mapping.Demapifier;
import de.quantummaid.messagemaid.mapping.Mapifier;
import de.quantummaid.messagemaid.mapping.SerializationFilters;
import de.quantummaid.messagemaid.messageBus.MessageBus;
import de.quantummaid.messagemaid.processingContext.EventType;
import de.quantummaid.messagemaid.serializedMessageBus.SerializedMessageBus;
import de.quantummaid.messagemaid.useCases.useCaseAdapter.LowLevelUseCaseAdapterBuilder;
import de.quantummaid.messagemaid.useCases.useCaseAdapter.UseCaseAdapter;
import de.quantummaid.messagemaid.useCases.useCaseAdapter.usecaseCalling.Caller;
import de.quantummaid.messagemaid.useCases.useCaseAdapter.usecaseInstantiating.UseCaseInstantiator;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

import static de.quantummaid.httpmaid.chains.MetaDataKey.metaDataKey;
import static de.quantummaid.httpmaid.events.EventModule.MESSAGE_BUS;
import static de.quantummaid.httpmaid.events.EventModule.eventModule;
import static de.quantummaid.httpmaid.handler.distribution.HandlerDistributors.HANDLER_DISTRIBUTORS;
import static de.quantummaid.httpmaid.util.Validators.validateNotNull;
import static de.quantummaid.messagemaid.internal.collections.filtermap.FilterMapBuilder.filterMapBuilder;
import static de.quantummaid.messagemaid.internal.collections.predicatemap.PredicateMapBuilder.predicateMapBuilder;
import static de.quantummaid.messagemaid.mapping.DeserializationFilters.areOfType;
import static de.quantummaid.messagemaid.mapping.ExceptionMapifier.defaultExceptionMapifier;
import static de.quantummaid.messagemaid.processingContext.EventType.eventTypeFromClass;
import static de.quantummaid.messagemaid.useCases.useCaseAdapter.usecaseCalling.SinglePublicUseCaseMethodCaller.singlePublicUseCaseMethodCaller;
import static de.quantummaid.messagemaid.useCases.useCaseAdapter.usecaseInstantiating.ZeroArgumentsConstructorUseCaseInstantiator.zeroArgumentsConstructorUseCaseInstantiator;
import static java.util.Collections.singletonList;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class UseCasesModule implements ChainModule {
    public static final MetaDataKey<SerializedMessageBus> SERIALIZED_MESSAGE_BUS = metaDataKey("SERIALIZED_MESSAGE_BUS");

    private UseCaseInstantiator useCaseInstantiator;
    private final Map<Class<?>, EventType> useCaseToEventMappings = new HashMap<>();
    private final FilterMapBuilder<Class<?>, Map<String, Object>, Demapifier<?>> requestDeserializers = filterMapBuilder();
    private final PredicateMapBuilder<Object, Mapifier<Object>> responseSerializers = predicateMapBuilder();
    private SerializerAndDeserializer serializerAndDeserializer;

    public static UseCasesModule useCasesModule() {
        return new UseCasesModule();
    }

    public void setUseCaseInstantiator(final UseCaseInstantiator useCaseInstantiator) {
        this.useCaseInstantiator = useCaseInstantiator;
    }

    public void addUseCaseToEventMapping(final Class<?> useCaseClass,
                                         final EventType eventType) {
        validateNotNull(useCaseClass, "useCaseClass");
        validateNotNull(eventType, "eventType");
        useCaseToEventMappings.put(useCaseClass, eventType);
    }

    @SuppressWarnings("unchecked")
    public <T> void addRequestMapperForType(final Class<T> type,
                                            final Demapifier<T> requestMapper) {
        addRequestMapper((clazz, event) -> areOfType(type).test(clazz, event), requestMapper);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void addRequestMapper(final EventFilter filter,
                                 final Demapifier<?> requestMapper) {
        validateNotNull(filter, "filter");
        validateNotNull(requestMapper, "requestMapper");
        requestDeserializers.put(filter::filter, requestMapper);
    }

    @SuppressWarnings("unchecked")
    public <T> void addResponseSerializerForType(final Class<T> type,
                                                 final Mapifier<T> responseMapper) {
        addResponseSerializer(SerializationFilters.areOfType(type), (Mapifier<Object>) responseMapper);
    }

    public void addResponseSerializer(final Predicate<Object> filter,
                                      final Mapifier<Object> responseMapper) {
        validateNotNull(filter, "filter");
        validateNotNull(responseMapper, "responseMapper");
        responseSerializers.put(filter, responseMapper);
    }

    public void setSerializerAndDeserializer(final SerializerAndDeserializer serializerAndDeserializer) {
        this.serializerAndDeserializer = serializerAndDeserializer;
    }

    @Override
    public List<ChainModule> supplyModulesIfNotAlreadyPreset() {
        return singletonList(eventModule());
    }

    @Override
    public void init(final MetaData configurationMetaData) {
        final HandlerDistributors handlerDistributors = configurationMetaData.get(HANDLER_DISTRIBUTORS);
        handlerDistributors.register(handler -> handler instanceof Class, (handler, condition) -> {
            final Class<?> useCaseClass = (Class<?>) handler;
            final EventType eventType = eventTypeFromClass(useCaseClass);
            useCaseToEventMappings.put(useCaseClass, eventType);
            handlerDistributors.distribute(eventType, condition);
        });
    }

    @SuppressWarnings("unchecked")
    @Override
    public void register(final ChainExtender extender) {
        final LowLevelUseCaseAdapterBuilder lowLevelUseCaseAdapterBuilder = LowLevelUseCaseAdapterBuilder.aLowLevelUseCaseInvocationBuilder();

        useCaseToEventMappings.forEach((useCase, eventType) -> lowLevelUseCaseAdapterBuilder.addUseCase(
                (Class<Object>) useCase,
                eventType,
                (Caller<Object>) singlePublicUseCaseMethodCaller(useCase)));

        if (useCaseInstantiator != null) {
            lowLevelUseCaseAdapterBuilder.setUseCaseInstantiator(useCaseInstantiator);
        } else {
            lowLevelUseCaseAdapterBuilder.setUseCaseInstantiator(zeroArgumentsConstructorUseCaseInstantiator());
        }
        this.requestDeserializers.setDefaultValue((targetType, map) -> serializerAndDeserializer.deserialize(targetType, map));
        lowLevelUseCaseAdapterBuilder.setRequestDeserializers(this.requestDeserializers);
        this.responseSerializers.put(Objects::isNull, object -> null);
        this.responseSerializers.setDefaultValue(object -> serializerAndDeserializer.serialize(object));
        lowLevelUseCaseAdapterBuilder.setReseponseSerializers(responseSerializers);

        lowLevelUseCaseAdapterBuilder.setRequestSerializers(failingPredicateMap());
        lowLevelUseCaseAdapterBuilder.setResponseDeserializers(failingFilterMap());

        final PredicateMapBuilder<Exception, Mapifier<Exception>> exceptionSerializers = predicateMapBuilder();
        exceptionSerializers.setDefaultValue(defaultExceptionMapifier());
        lowLevelUseCaseAdapterBuilder.setExceptionSerializers(exceptionSerializers);

        final UseCaseAdapter useCaseAdapter = lowLevelUseCaseAdapterBuilder.build();
        final MessageBus messageBus = extender.getMetaDatum(MESSAGE_BUS);

        final SerializedMessageBus serializedMessageBus = useCaseAdapter.attachAndEnhance(messageBus);
        extender.addMetaDatum(SERIALIZED_MESSAGE_BUS, serializedMessageBus);
    }

    private static FilterMapBuilder<Class<?>, Map<String, Object>, Demapifier<?>> failingFilterMap() {
        final FilterMapBuilder<Class<?>, Map<String, Object>, Demapifier<?>> filterMap = filterMapBuilder();
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
}
