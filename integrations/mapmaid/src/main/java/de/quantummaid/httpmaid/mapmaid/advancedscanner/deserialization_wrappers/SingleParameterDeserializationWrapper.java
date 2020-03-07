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

package de.quantummaid.httpmaid.mapmaid.advancedscanner.deserialization_wrappers;

import de.quantummaid.mapmaid.MapMaid;
import de.quantummaid.mapmaid.builder.recipes.advancedscanner.deserialization_wrappers.MethodParameterDeserializationWrapper;
import de.quantummaid.mapmaid.shared.types.ResolvedType;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Map;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class SingleParameterDeserializationWrapper implements MethodParameterDeserializationWrapper {
    private final String name;
    private final ResolvedType type;

    public static MethodParameterDeserializationWrapper singleParameter(final String name, final ResolvedType type) {
        return new SingleParameterDeserializationWrapper(name, type);
    }

    @Override
    public Map<String, Object> deserializeParameters(final Object input, final MapMaid mapMaid) {
        final Object value = mapMaid.deserializer().deserializeFromUniversalObject(input, this.type);
        return Map.of(this.name, value);
    }
}
