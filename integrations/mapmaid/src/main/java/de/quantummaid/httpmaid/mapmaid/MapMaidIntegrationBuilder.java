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

import de.quantummaid.httpmaid.chains.ConfiguratorBuilder;
import de.quantummaid.httpmaid.http.headers.ContentType;
import de.quantummaid.mapmaid.MapMaid;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import static de.quantummaid.httpmaid.mapmaid.MapMaidSerializerAndDeserializer.mapMaidSerializerAndDeserializer;
import static de.quantummaid.httpmaid.util.Validators.validateNotNull;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class MapMaidIntegrationBuilder implements ConfiguratorBuilder {
    private final MapMaidSerializerAndDeserializer mapMaidSerializerAndDeserializer;

    static MapMaidIntegrationBuilder mapMaidIntegration(final MapMaid mapMaid) {
        validateNotNull(mapMaid, "mapMaid");
        final MapMaidSerializerAndDeserializer mapMaidSerializerAndDeserializer = mapMaidSerializerAndDeserializer();
        mapMaidSerializerAndDeserializer.setMapMaid(mapMaid);
        return new MapMaidIntegrationBuilder(mapMaidSerializerAndDeserializer);
    }

    public MarshallerTypeStage<MapMaidIntegrationBuilder> matchingTheContentType(final ContentType contentType) {
        validateNotNull(contentType, "contentType");
        return marshallingType -> {
            validateNotNull(marshallingType, "marshallingType");
            mapMaidSerializerAndDeserializer.addRequestContentTypeToUnmarshallingTypeMapping(contentType, marshallingType);
            mapMaidSerializerAndDeserializer.addMarshallingTypeToResponseContentTypeMapping(contentType, marshallingType);
            return this;
        };
    }

    public MapMaidIntegrationBuilder assumingTheDefaultContentType(final ContentType defaultContentType) {
        validateNotNull(defaultContentType, "defaultContentType");
        mapMaidSerializerAndDeserializer.setDefaultContentType(defaultContentType);
        return this;
    }

    @Override
    public MapMaidSerializerAndDeserializer build() {
        return mapMaidSerializerAndDeserializer;
    }
}
