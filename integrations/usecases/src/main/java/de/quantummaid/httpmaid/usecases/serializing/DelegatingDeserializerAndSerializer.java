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

package de.quantummaid.httpmaid.usecases.serializing;

import de.quantummaid.eventmaid.mapping.Demapifier;
import de.quantummaid.eventmaid.mapping.Mapifier;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import static de.quantummaid.httpmaid.util.Validators.validateNotNull;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class DelegatingDeserializerAndSerializer implements SerializerAndDeserializer {
    private final Demapifier<Object> requestMapper;
    private final Mapifier<Object> responseMapper;

    public static SerializerAndDeserializer delegatingDeserializerAndSerializer(final Demapifier<Object> requestMapper,
                                                                                final Mapifier<Object> responseMapper) {
        validateNotNull(requestMapper, "requestMapper");
        validateNotNull(responseMapper, "responseMapper");
        return new DelegatingDeserializerAndSerializer(requestMapper, responseMapper);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T deserialize(final Class<T> type,
                             final Object map) {
        return (T) requestMapper.map((Class<Object>) type, map);
    }

    @Override
    public Object serialize(final Object event) {
        return responseMapper.map(event);
    }
}
