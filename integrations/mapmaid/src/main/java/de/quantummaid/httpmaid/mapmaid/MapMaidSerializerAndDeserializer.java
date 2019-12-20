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
import de.quantummaid.httpmaid.chains.ChainModule;
import de.quantummaid.httpmaid.chains.DependencyRegistry;
import de.quantummaid.httpmaid.http.headers.ContentType;
import de.quantummaid.httpmaid.marshalling.MarshallingModule;
import de.quantummaid.httpmaid.usecases.UseCasesModule;
import de.quantummaid.httpmaid.usecases.serializing.SerializerAndDeserializer;
import de.quantummaid.mapmaid.MapMaid;
import de.quantummaid.mapmaid.mapper.deserialization.validation.AggregatedValidationException;
import de.quantummaid.mapmaid.mapper.marshalling.MarshallingType;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.quantummaid.httpmaid.HttpMaidChainKeys.RESPONSE_BODY_MAP;
import static de.quantummaid.httpmaid.marshalling.MarshallingModule.emptyMarshallingModule;
import static de.quantummaid.httpmaid.util.Validators.validateNotNull;
import static de.quantummaid.mapmaid.builder.recipes.marshallers.urlencoded.UrlEncodedMarshallerRecipe.urlEncoded;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Map.of;
import static java.util.stream.Collectors.toList;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class MapMaidSerializerAndDeserializer implements SerializerAndDeserializer {
    private static final Map<MarshallingType, ContentType> DEFAULT_CONTENT_TYPE_MAPPINGS = of(
            MarshallingType.json(), ContentType.json(),
            MarshallingType.xml(), ContentType.xml(),
            MarshallingType.yaml(), ContentType.yaml(),
            urlEncoded(), ContentType.formUrlEncoded()
    );
    private static final List<MarshallingType> DEFAULT_SUPPORTED_TYPES_FOR_UNMARSHALLING = asList(
            MarshallingType.json(), MarshallingType.xml(), MarshallingType.yaml(), urlEncoded());
    private static final List<MarshallingType> DEFAULT_SUPPORTED_TYPES_FOR_MARSHALLING = asList(
            MarshallingType.json(), MarshallingType.xml(), MarshallingType.yaml());

    private volatile MapMaid mapMaid;
    private volatile ContentType defaultContentType;
    private volatile boolean addAggregatedExceptionHandler = true;

    private final Map<ContentType, MarshallingType> contentTypeMappingsForUnmarshalling = new HashMap<>();
    private final Map<ContentType, MarshallingType> contentTypeMappingsForMarshalling = new HashMap<>();

    static MapMaidSerializerAndDeserializer mapMaidSerializerAndDeserializer() {
        return new MapMaidSerializerAndDeserializer();
    }

    public void setMapMaid(final MapMaid mapMaid) {
        validateNotNull(mapMaid, "mapMaid");
        this.mapMaid = mapMaid;
    }

    public void setDefaultContentType(final ContentType defaultContentType) {
        validateNotNull(defaultContentType, "defaultContentType");
        this.defaultContentType = defaultContentType;
    }

    public void doNotAddAggregatedExceptionHandler() {
        this.addAggregatedExceptionHandler = false;
    }

    public void addRequestContentTypeToUnmarshallingTypeMapping(final ContentType contentType,
                                                                final MarshallingType marshallingType) {
        validateNotNull(contentType, "contentType");
        validateNotNull(marshallingType, "marshallingType");
        contentTypeMappingsForUnmarshalling.put(contentType, marshallingType);
    }

    public void addMarshallingTypeToResponseContentTypeMapping(final ContentType contentType,
                                                               final MarshallingType marshallingType) {
        validateNotNull(contentType, "contentType");
        validateNotNull(marshallingType, "marshallingType");
        contentTypeMappingsForMarshalling.put(contentType, marshallingType);
    }

    @Override
    public <T> T deserialize(final Class<T> type, final Map<String, Object> map) {
        return mapMaid.deserializer().deserializeFromMap(map, type);
    }

    @Override
    public Map<String, Object> serialize(final Object event) {
        return mapMaid.serializer().serializeToMap(event);
    }

    @Override
    public List<ChainModule> supplyModulesIfNotAlreadyPreset() {
        return singletonList(emptyMarshallingModule());
    }

    @Override
    public void configure(final DependencyRegistry dependencyRegistry) {
        mapMaid.deserializer().supportedMarshallingTypes().stream()
                .filter(marshallingType -> !contentTypeMappingsForUnmarshalling.containsValue(marshallingType))
                .filter(DEFAULT_SUPPORTED_TYPES_FOR_UNMARSHALLING::contains)
                .forEach(marshallingType -> {
                    final ContentType contentType = DEFAULT_CONTENT_TYPE_MAPPINGS.get(marshallingType);
                    contentTypeMappingsForUnmarshalling.put(contentType, marshallingType);
                });

        final MarshallingModule marshallingModule = dependencyRegistry.getDependency(MarshallingModule.class);
        contentTypeMappingsForUnmarshalling.forEach((contentType, marshallingType) -> marshallingModule
                .addUnmarshaller(contentType, input -> mapMaid.deserializer().deserializeToMap(input, marshallingType)));

        mapMaid.serializer().supportedMarshallingTypes().stream()
                .filter(marshallingType -> !contentTypeMappingsForMarshalling.containsValue(marshallingType))
                .filter(DEFAULT_SUPPORTED_TYPES_FOR_MARSHALLING::contains)
                .forEach(marshallingType -> {
                    final ContentType contentType = DEFAULT_CONTENT_TYPE_MAPPINGS.get(marshallingType);
                    contentTypeMappingsForMarshalling.put(contentType, marshallingType);
                });
        contentTypeMappingsForMarshalling.forEach((contentType, marshallingType) -> marshallingModule
                .addMarshaller(contentType, map -> mapMaid.serializer().serializeFromMap(map, marshallingType)));

        dependencyRegistry.getDependency(UseCasesModule.class).setSerializerAndDeserializer(this);

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
}
