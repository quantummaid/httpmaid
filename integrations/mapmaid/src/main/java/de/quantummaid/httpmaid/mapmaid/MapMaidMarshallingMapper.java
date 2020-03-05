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

import de.quantummaid.httpmaid.http.headers.ContentType;
import de.quantummaid.httpmaid.marshalling.MarshallingModule;
import de.quantummaid.mapmaid.MapMaid;
import de.quantummaid.mapmaid.mapper.marshalling.MarshallingType;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.quantummaid.httpmaid.util.Validators.validateNotNull;
import static de.quantummaid.mapmaid.builder.recipes.marshallers.urlencoded.UrlEncodedMarshallerRecipe.urlEncoded;
import static java.util.Arrays.asList;
import static java.util.Map.of;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
final class MapMaidMarshallingMapper {
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

    private final Map<ContentType, MarshallingType> contentTypeMappingsForUnmarshalling = new HashMap<>();
    private final Map<ContentType, MarshallingType> contentTypeMappingsForMarshalling = new HashMap<>();

    static MapMaidMarshallingMapper mapMaidMarshallingMapper() {
        return new MapMaidMarshallingMapper();
    }

    void addRequestContentTypeToUnmarshallingTypeMapping(final ContentType contentType,
                                                         final MarshallingType marshallingType) {
        validateNotNull(contentType, "contentType");
        validateNotNull(marshallingType, "marshallingType");
        contentTypeMappingsForUnmarshalling.put(contentType, marshallingType);
    }

    void addMarshallingTypeToResponseContentTypeMapping(final ContentType contentType,
                                                        final MarshallingType marshallingType) {
        validateNotNull(contentType, "contentType");
        validateNotNull(marshallingType, "marshallingType");
        contentTypeMappingsForMarshalling.put(contentType, marshallingType);
    }

    void mapMarshalling(final MapMaid mapMaid, final MarshallingModule marshallingModule) {
        mapMaid.deserializer().supportedMarshallingTypes().stream()
                .filter(marshallingType -> !contentTypeMappingsForUnmarshalling.containsValue(marshallingType))
                .filter(DEFAULT_SUPPORTED_TYPES_FOR_UNMARSHALLING::contains)
                .forEach(marshallingType -> {
                    final ContentType contentType = DEFAULT_CONTENT_TYPE_MAPPINGS.get(marshallingType);
                    contentTypeMappingsForUnmarshalling.put(contentType, marshallingType);
                });

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
                .addMarshaller(contentType, map -> mapMaid.serializer().serializeFromUniversalObject(map, marshallingType)));
    }
}
