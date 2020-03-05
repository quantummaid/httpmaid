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

package de.quantummaid.httpmaid.usecases;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Collection;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class UseCases {
    private final Collection<Class<?>> useCases;
    private final Collection<Class<?>> typesWithSpecialSerializers;
    private final Collection<Class<?>> typesWithSpecialDeserializers;

    public static UseCases useCases(final Collection<Class<?>> useCases,
                                    final Collection<Class<?>> typesWithSpecialSerializers,
                                    final Collection<Class<?>> typesWithSpecialDeserializers) {
        return new UseCases(useCases, typesWithSpecialSerializers, typesWithSpecialDeserializers);
    }

    public Collection<Class<?>> useCases() {
        return useCases;
    }

    public Collection<Class<?>> typesWithSpecialSerializers() {
        return typesWithSpecialSerializers;
    }

    public Collection<Class<?>> typesWithSpecialDeserializers() {
        return typesWithSpecialDeserializers;
    }
}
