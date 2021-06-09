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

import de.quantummaid.httpmaid.events.enriching.Injection;
import de.quantummaid.mapmaid.MapMaid;
import de.quantummaid.reflectmaid.typescanner.TypeIdentifier;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.List;
import java.util.Map;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class MultipleParametersDeserializationWrapper implements MethodParameterDeserializationWrapper {
    private final TypeIdentifier typeIdentifier;

    public static MethodParameterDeserializationWrapper multipleParameters(final TypeIdentifier typeIdentifier) {
        return new MultipleParametersDeserializationWrapper(typeIdentifier);
    }

    @Override
    public Map<String, Object> deserializeParameters(final Map<String, Object> input,
                                                     final List<Injection> injections,
                                                     final List<Object> typeInjections,
                                                     final MapMaid mapMaid) {
        return mapMaid.deserializeFromUniversalObject(
                input,
                this.typeIdentifier,
                injector -> {
                    injections.forEach(injection -> {
                        final String key = injection.key();
                        final String value = injection.value();
                        injector.put(key, value);
                    });
                    typeInjections.forEach(injector::put);
                }
        );
    }
}
