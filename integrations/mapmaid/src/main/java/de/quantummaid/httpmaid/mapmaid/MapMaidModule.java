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

package de.quantummaid.httpmaid.mapmaid;

import de.quantummaid.httpmaid.CoreModule;
import de.quantummaid.httpmaid.chains.ChainExtender;
import de.quantummaid.httpmaid.chains.ChainModule;
import de.quantummaid.httpmaid.chains.DependencyRegistry;
import de.quantummaid.httpmaid.http.headers.ContentType;
import de.quantummaid.httpmaid.marshalling.MarshallingModule;
import de.quantummaid.httpmaid.usecases.UseCasesModule;
import de.quantummaid.httpmaid.usecases.serializing.SerializerAndDeserializer;
import de.quantummaid.mapmaid.MapMaid;
import de.quantummaid.mapmaid.builder.MapMaidBuilder;
import de.quantummaid.mapmaid.mapper.deserialization.validation.AggregatedValidationException;
import de.quantummaid.mapmaid.mapper.marshalling.MarshallingType;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.*;

import static de.quantummaid.httpmaid.HttpMaidChainKeys.RESPONSE_BODY_MAP;
import static de.quantummaid.httpmaid.mapmaid.MapMaidConfigurators.RECIPES;
import static de.quantummaid.httpmaid.mapmaid.MapMaidMarshallingMapper.mapMaidMarshallingMapper;
import static de.quantummaid.httpmaid.mapmaid.MapMaidSerializerAndDeserializer.mapMaidSerializerAndDeserializer;
import static de.quantummaid.httpmaid.marshalling.MarshallingModule.emptyMarshallingModule;
import static de.quantummaid.httpmaid.util.Validators.validateNotNull;
import static de.quantummaid.mapmaid.MapMaid.aMapMaid;
import static de.quantummaid.mapmaid.builder.recipes.scanner.ClassScannerRecipe.addAllReferencedClassesIs;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class MapMaidModule implements ChainModule {
    private volatile MapMaid mapMaid;
    private final MapMaidMarshallingMapper mapMaidMarshallingMapper = mapMaidMarshallingMapper();
    private volatile boolean addAggregatedExceptionHandler = true;

    public static MapMaidModule mapMaidModule() {
        return new MapMaidModule();
    }

    public void setMapMaid(final MapMaid mapMaid) {
        validateNotNull(mapMaid, "mapMaid");
        this.mapMaid = mapMaid;
    }

    public void doNotAddAggregatedExceptionHandler() {
        this.addAggregatedExceptionHandler = false;
    }

    public void addRequestContentTypeToUnmarshallingTypeMapping(final ContentType contentType,
                                                                final MarshallingType marshallingType) {

        mapMaidMarshallingMapper.addRequestContentTypeToUnmarshallingTypeMapping(contentType, marshallingType);
    }

    public void addMarshallingTypeToResponseContentTypeMapping(final ContentType contentType,
                                                               final MarshallingType marshallingType) {
        mapMaidMarshallingMapper.addMarshallingTypeToResponseContentTypeMapping(contentType, marshallingType);
    }

    @Override
    public List<ChainModule> supplyModulesIfNotAlreadyPreset() {
        return singletonList(emptyMarshallingModule());
    }

    @Override
    public void configure(final DependencyRegistry dependencyRegistry) {
        final UseCasesModule useCasesModule = dependencyRegistry.getDependency(UseCasesModule.class);
        final Set<Class<?>> useCases = useCasesModule.getUseCases();

        if (mapMaid == null) {
            final MapMaidBuilder mapMaidBuilder = aMapMaid();
            dependencyRegistry.getMetaData().getOrSetDefault(RECIPES, LinkedList::new).forEach(mapMaidBuilder::usingRecipe);
            useCases.forEach(useCase -> mapMaidBuilder.usingRecipe(addAllReferencedClassesIs(useCase)));
            mapMaid = mapMaidBuilder.build();
        }

        final MarshallingModule marshallingModule = dependencyRegistry.getDependency(MarshallingModule.class);
        mapMaidMarshallingMapper.mapMarshalling(mapMaid, marshallingModule);

        final SerializerAndDeserializer serializerAndDeserializer = mapMaidSerializerAndDeserializer(mapMaid);
        useCasesModule.setSerializerAndDeserializer(serializerAndDeserializer);

        if (addAggregatedExceptionHandler) {
            final CoreModule coreModule = dependencyRegistry.getDependency(CoreModule.class);
            coreModule.addExceptionMapper(throwable -> throwable instanceof AggregatedValidationException,
                    (exception, metaData) -> {
                        final AggregatedValidationException aggregatedException = (AggregatedValidationException) exception;
                        final List<Object> errorsList = aggregatedException.getValidationErrors()
                                .stream()
                                .map(validationError -> Map.of(
                                        "message", validationError.message,
                                        "path", validationError.propertyPath))
                                .collect(toList());
                        metaData.set(RESPONSE_BODY_MAP, Map.of("errors", errorsList));
                    });
        }
    }

    @Override
    public void register(final ChainExtender extender) {
    }
}
