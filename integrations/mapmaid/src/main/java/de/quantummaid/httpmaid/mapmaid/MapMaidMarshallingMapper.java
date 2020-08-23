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
import de.quantummaid.mapmaid.mapper.marshalling.Unmarshaller;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.quantummaid.httpmaid.http.headers.ContentType.formUrlEncoded;
import static de.quantummaid.httpmaid.util.Validators.validateNotNull;
import static de.quantummaid.mapmaid.builder.recipes.urlencoded.UrlEncodedMarshallerRecipe.urlEncoded;
import static de.quantummaid.mapmaid.builder.recipes.urlencoded.UrlEncodedUnmarshaller.urlEncodedUnmarshaller;
import static de.quantummaid.mapmaid.mapper.marshalling.MarshallingType.JSON;
import static java.util.Arrays.asList;
import static java.util.Map.of;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
final class MapMaidMarshallingMapper {
    private static final Map<MarshallingType<String>, ContentType> DEFAULT_CONTENT_TYPE_MAPPINGS = of(
            JSON, ContentType.json(),
            MarshallingType.XML, ContentType.xml(),
            MarshallingType.YAML, ContentType.yaml(),
            urlEncoded(), formUrlEncoded()
    );
    private static final List<MarshallingType<String>> DEFAULT_SUPPORTED_TYPES_FOR_UNMARSHALLING = asList(
            JSON, MarshallingType.XML, MarshallingType.YAML, urlEncoded());
    private static final List<MarshallingType<String>> DEFAULT_SUPPORTED_TYPES_FOR_MARSHALLING = asList(
            JSON, MarshallingType.XML, MarshallingType.YAML);

    private final Map<ContentType, MarshallingType<String>> contentTypeMappingsForUnmarshalling = new HashMap<>();
    private final Map<ContentType, MarshallingType<String>> contentTypeMappingsForMarshalling = new HashMap<>();

    static MapMaidMarshallingMapper mapMaidMarshallingMapper() {
        return new MapMaidMarshallingMapper();
    }

    void addRequestContentTypeToUnmarshallingTypeMapping(final ContentType contentType,
                                                         final MarshallingType<String> marshallingType) {
        validateNotNull(contentType, "contentType");
        validateNotNull(marshallingType, "marshallingType");
        contentTypeMappingsForUnmarshalling.put(contentType, marshallingType);
    }

    void addMarshallingTypeToResponseContentTypeMapping(final ContentType contentType,
                                                        final MarshallingType<String> marshallingType) {
        validateNotNull(contentType, "contentType");
        validateNotNull(marshallingType, "marshallingType");
        contentTypeMappingsForMarshalling.put(contentType, marshallingType);
    }

    @SuppressWarnings("unchecked")
    void mapMarshalling(final MapMaid mapMaid, final MarshallingModule marshallingModule) {
        final Unmarshaller<String> urlEncodedMarshaller = urlEncodedUnmarshaller();
        marshallingModule.addUnmarshaller(formUrlEncoded(), urlEncodedMarshaller::unmarshal);

        mapMaid.deserializer().supportedMarshallingTypes().stream()
                .map(marshallingType -> (MarshallingType<String>) marshallingType)
                .filter(marshallingType -> !contentTypeMappingsForUnmarshalling.containsValue(marshallingType))
                .filter(DEFAULT_SUPPORTED_TYPES_FOR_UNMARSHALLING::contains)
                .forEach(marshallingType -> {
                    final ContentType contentType = DEFAULT_CONTENT_TYPE_MAPPINGS.get(marshallingType);
                    contentTypeMappingsForUnmarshalling.put(contentType, marshallingType);
                });

        contentTypeMappingsForUnmarshalling.forEach((contentType, marshallingType) -> marshallingModule
                .addUnmarshaller(contentType, input -> mapMaid.deserializer().deserializeToUniversalObject(input, marshallingType)));

        mapMaid.serializer().supportedMarshallingTypes().stream()
                .map(marshallingType -> (MarshallingType<String>) marshallingType)
                .filter(marshallingType -> !contentTypeMappingsForMarshalling.containsValue(marshallingType))
                .filter(DEFAULT_SUPPORTED_TYPES_FOR_MARSHALLING::contains)
                .forEach(marshallingType -> {
                    final ContentType contentType = DEFAULT_CONTENT_TYPE_MAPPINGS.get(marshallingType);
                    contentTypeMappingsForMarshalling.put(contentType, marshallingType);
                });
        contentTypeMappingsForMarshalling.forEach((contentType, marshallingType) -> marshallingModule
                .addMarshaller(contentType, map -> mapMaid.serializer().marshalFromUniversalObject(map, marshallingType)));

        if (mapMaid.deserializer().supportedMarshallingTypes().contains(JSON)) {
            marshallingModule.setDefaultContentTypeProvider(ContentType.json());
        }
    }
}
