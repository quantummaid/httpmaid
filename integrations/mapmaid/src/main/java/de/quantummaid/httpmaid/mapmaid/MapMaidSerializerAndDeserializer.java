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

import de.quantummaid.httpmaid.usecases.serializing.SerializerAndDeserializer;
import de.quantummaid.mapmaid.MapMaid;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import static de.quantummaid.httpmaid.util.Validators.validateNotNull;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class MapMaidSerializerAndDeserializer implements SerializerAndDeserializer {
    private final MapMaid mapMaid;

    public static SerializerAndDeserializer mapMaidSerializerAndDeserializer(final MapMaid mapMaid) {
        validateNotNull(mapMaid, "mapMaid");
        return new MapMaidSerializerAndDeserializer(mapMaid);
    }

    @Override
    public <T> T deserialize(final Class<T> type, final Object map) {
        return mapMaid.deserializer().deserializeFromUniversalObject(map, type);
    }

    @Override
    public Object serialize(final Object event) {
        return mapMaid.serializer().serializeToUniversalObject(event);
    }
}
