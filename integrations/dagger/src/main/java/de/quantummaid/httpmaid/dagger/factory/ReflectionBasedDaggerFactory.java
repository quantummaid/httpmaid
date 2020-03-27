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

package de.quantummaid.httpmaid.dagger.factory;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static de.quantummaid.httpmaid.dagger.DaggerIntegrationException.daggerIntegrationException;
import static java.lang.String.format;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ReflectionBasedDaggerFactory implements DaggerFactory {
    private final Method factoryMethod;
    private final Method factoryConstructor;
    private final Class<?> factoryClass;

    public static DaggerFactory reflectionBasedDaggerFactory(final Method factoryMethod,
                                                             final Method factoryConstructor,
                                                             final Class<?> factoryClass) {
        return new ReflectionBasedDaggerFactory(factoryMethod, factoryConstructor, factoryClass);
    }

    @Override
    public Object instantiate() {
        final Object factory = invokeFactoryConstructor();
        return invokeFactoryMethod(factory);
    }

    private Object invokeFactoryConstructor() {
        try {
            return factoryConstructor.invoke(null);
        } catch (final IllegalAccessException | InvocationTargetException e) {
            throw daggerIntegrationException(format(
                    "Failed to instantiate Dagger factory '%s' using method '%s'",
                    factoryClass.getName(), factoryConstructor.getName()), e);
        }
    }

    private Object invokeFactoryMethod(final Object factory) {
        try {
            return factoryMethod.invoke(factory);
        } catch (final IllegalAccessException | InvocationTargetException e) {
            throw daggerIntegrationException(format(
                    "Invocation of Dagger factory method '%s' in class '%s'",
                    factoryMethod.getName(), factoryClass.getName()), e);
        }
    }
}
