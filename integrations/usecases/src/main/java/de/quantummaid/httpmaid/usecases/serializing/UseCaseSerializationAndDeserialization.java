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

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Map;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class UseCaseSerializationAndDeserialization {
    private final Map<Class<?>, UseCaseParamaterProvider> parameterProviders;
    private final UseCaseReturnValueSerializer returnValueSerializer;

    public static UseCaseSerializationAndDeserialization useCaseSerializationAndDeserialization(
            final Map<Class<?>, UseCaseParamaterProvider> parameterProviders,
            final UseCaseReturnValueSerializer returnValueSerializer) {
        return new UseCaseSerializationAndDeserialization(parameterProviders, returnValueSerializer);
    }

    public UseCaseReturnValueSerializer returnValueSerializer() {
        return returnValueSerializer;
    }

    public Map<String, Object> deserializeParameters(final Object event, final Class<?> useCaseClass) {
        final UseCaseParamaterProvider useCaseParamaterProvider = parameterProviders.get(useCaseClass);
        return useCaseParamaterProvider.provideParameters(event);
    }

    public Object serializeReturnValue(final Object returnValue) {
        return this.returnValueSerializer.serialize(returnValue);
    }
}
