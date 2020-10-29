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

package de.quantummaid.httpmaid.mapmaid;

import de.quantummaid.httpmaid.CoreModule;
import de.quantummaid.httpmaid.chains.ChainExtender;
import de.quantummaid.httpmaid.chains.ChainModule;
import de.quantummaid.httpmaid.chains.DependencyRegistry;
import de.quantummaid.httpmaid.events.enriching.Injection;
import de.quantummaid.httpmaid.http.headers.ContentType;
import de.quantummaid.httpmaid.mapmaid.advancedscanner.deserialization_wrappers.MethodParameterDeserializationWrapper;
import de.quantummaid.httpmaid.marshalling.MarshallingModule;
import de.quantummaid.httpmaid.serialization.Serializer;
import de.quantummaid.httpmaid.usecases.UseCasesModule;
import de.quantummaid.httpmaid.usecases.serializing.SerializationAndDeserializationProvider;
import de.quantummaid.httpmaid.usecases.serializing.UseCaseParamaterProvider;
import de.quantummaid.mapmaid.MapMaid;
import de.quantummaid.mapmaid.builder.MapMaidBuilder;
import de.quantummaid.mapmaid.mapper.deserialization.validation.AggregatedValidationException;
import de.quantummaid.mapmaid.mapper.marshalling.MarshallingType;
import de.quantummaid.reflectmaid.ResolvedType;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.quantummaid.httpmaid.mapmaid.MapMaidMarshallingMapper.mapMaidMarshallingMapper;
import static de.quantummaid.httpmaid.mapmaid.MapMaidValidationExceptionMapper.mapMaidValidationExceptionMapper;
import static de.quantummaid.httpmaid.mapmaid.advancedscanner.UseCaseClassScanner.addAllReferencedClassesIn;
import static de.quantummaid.httpmaid.marshalling.MarshallingModule.emptyMarshallingModule;
import static de.quantummaid.httpmaid.usecases.serializing.UseCaseSerializationAndDeserialization.useCaseSerializationAndDeserialization;
import static de.quantummaid.mapmaid.MapMaid.aMapMaid;
import static de.quantummaid.mapmaid.shared.identifier.TypeIdentifier.typeIdentifierFor;
import static de.quantummaid.reflectmaid.GenericType.fromResolvedType;
import static java.util.Collections.singletonList;

@Slf4j
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class MapMaidModule implements ChainModule {
    private static final int DEFAULT_VALIDATION_ERROR_STATUS_CODE = 400;

    private final MapMaidMarshallingMapper mapMaidMarshallingMapper = mapMaidMarshallingMapper();
    private volatile boolean addAggregatedExceptionHandler = true;
    private volatile int validationErrorStatusCode = DEFAULT_VALIDATION_ERROR_STATUS_CODE;

    public static MapMaidModule mapMaidModule() {
        return new MapMaidModule();
    }

    public void doNotAddAggregatedExceptionHandler() {
        this.addAggregatedExceptionHandler = false;
    }

    public void addRequestContentTypeToUnmarshallingTypeMapping(final ContentType contentType,
                                                                final MarshallingType<String> marshallingType) {

        mapMaidMarshallingMapper.addRequestContentTypeToUnmarshallingTypeMapping(contentType, marshallingType);
    }

    public void addMarshallingTypeToResponseContentTypeMapping(final ContentType contentType,
                                                               final MarshallingType<String> marshallingType) {
        mapMaidMarshallingMapper.addMarshallingTypeToResponseContentTypeMapping(contentType, marshallingType);
    }

    public void setValidationErrorStatusCode(final int validationErrorStatusCode) {
        this.validationErrorStatusCode = validationErrorStatusCode;
    }

    @Override
    public List<ChainModule> supplyModulesIfNotAlreadyPresent() {
        return singletonList(emptyMarshallingModule());
    }

    @Override
    public void configure(final DependencyRegistry dependencyRegistry) {
        final UseCasesModule useCasesModule = dependencyRegistry.getDependency(UseCasesModule.class);

        final SerializationAndDeserializationProvider serializationAndDeserializationProvider =
                (useCaseMethods, injectionTypes, messageTypes) -> {
                    final MapMaidBuilder mapMaidBuilder = aMapMaid();
                    injectionTypes.forEach(mapMaidBuilder::injecting);
                    messageTypes.forEach(mapMaidBuilder::serializing);

                    final Map<ResolvedType, MethodParameterDeserializationWrapper> deserializationWrappers =
                            addAllReferencedClassesIn(useCaseMethods, mapMaidBuilder);

                    dependencyRegistry.getMetaData().getOptional(MapMaidConfigurators.RECIPES)
                            .ifPresent(recipes -> recipes.forEach(mapMaidBuilder::usingRecipe));

                    final MapMaid mapMaid = buildMapMaid(mapMaidBuilder);
                    final Map<ResolvedType, UseCaseParamaterProvider> parameterProviders = new HashMap<>();
                    deserializationWrappers.forEach((type, wrapper) ->
                            parameterProviders.put(type, event -> {
                                final Map<String, Object> map = event.asMap();
                                final List<Injection> injections = event.injections();
                                final List<Object> typeInjections = event.typeInjections();
                                return wrapper.deserializeParameters(map, injections, typeInjections, mapMaid);
                            }));

                    final MarshallingModule marshallingModule = dependencyRegistry.getDependency(MarshallingModule.class);
                    mapMaidMarshallingMapper.mapMarshalling(mapMaid, marshallingModule);

                    final Serializer serializer = (returnValue, type) ->
                            mapMaid.serializer().serializeToUniversalObject(returnValue, typeIdentifierFor(fromResolvedType(type)));
                    return useCaseSerializationAndDeserialization(parameterProviders, serializer);
                };
        useCasesModule.setSerializationAndDeserializationProvider(serializationAndDeserializationProvider);

        if (addAggregatedExceptionHandler) {
            addExceptionHandler(dependencyRegistry);
        }
    }

    private void addExceptionHandler(final DependencyRegistry dependencyRegistry) {
        final CoreModule coreModule = dependencyRegistry.getDependency(CoreModule.class);
        coreModule.addExceptionMapper(throwable -> throwable instanceof AggregatedValidationException,
                mapMaidValidationExceptionMapper(validationErrorStatusCode));
    }

    private MapMaid buildMapMaid(final MapMaidBuilder builder) {
        final long startTime = System.currentTimeMillis();
        final MapMaid mapMaid = builder.build();
        final long endTime = System.currentTimeMillis();
        final long time = endTime - startTime;
        if (log.isInfoEnabled()) {
            log.info("construction of MapMaid took {}ms", time);
        }
        return mapMaid;
    }

    @Override
    public void register(final ChainExtender extender) {
        // do nothing
    }
}
