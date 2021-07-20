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

package de.quantummaid.httpmaid.usecases.mapmaid;

import de.quantummaid.httpmaid.serialization.Serializer;
import de.quantummaid.mapmaid.MapMaid;
import de.quantummaid.mapmaid.mapper.marshalling.MarshallingType;
import de.quantummaid.reflectmaid.resolvedtype.ResolvedType;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import static de.quantummaid.reflectmaid.GenericType.fromResolvedType;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ReturnValueSerializer implements Serializer {
    private final MapMaid mapMaid;

    public static ReturnValueSerializer returnValueSerializer(final MapMaid mapMaid) {
        return new ReturnValueSerializer(mapMaid);
    }

    @Override
    public Object serialize(final Object object, final ResolvedType type) {
        final MarshallingType<Object> marshallingType = MarshallingType.UNIVERSAL_OBJECT;
        return mapMaid.serializeTo(object, marshallingType, fromResolvedType(type));
    }
}
