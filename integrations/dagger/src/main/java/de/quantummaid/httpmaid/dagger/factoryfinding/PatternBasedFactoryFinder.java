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

package de.quantummaid.httpmaid.dagger.factoryfinding;

import de.quantummaid.httpmaid.dagger.factory.DaggerFactory;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.lang.reflect.Method;

import static de.quantummaid.httpmaid.dagger.DaggerIntegrationException.daggerIntegrationException;
import static de.quantummaid.httpmaid.dagger.factory.ReflectionBasedDaggerFactory.reflectionBasedDaggerFactory;
import static java.lang.String.format;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class PatternBasedFactoryFinder implements DaggerFactoryFinder {
    private final NamePattern factoryClassNamePattern;
    private final String factoryConstructorName;
    private final NamePattern factoryMethodNamePattern;

    public static PatternBasedFactoryFinder patternBasedFactoryFinder(final NamePattern factoryClassNamePattern,
                                                                      final String factoryConstructorName,
                                                                      final NamePattern factoryMethodNamePattern) {
        return new PatternBasedFactoryFinder(factoryClassNamePattern, factoryConstructorName, factoryMethodNamePattern);
    }

    @Override
    public DaggerFactory findFactory(final Class<?> type) {
        final String factoryClassName = factoryClassNamePattern.fromClass(type);
        final Class<?> factoryClass = findFactoryClassByName(factoryClassName, type);
        final Method factoryConstructor = methodByName(factoryConstructorName, factoryClass);
        final String factoryMethodName = factoryMethodNamePattern.fromClass(type);
        final Method factoryMethod = methodByName(factoryMethodName, factoryClass);
        return reflectionBasedDaggerFactory(factoryMethod, factoryConstructor, factoryClass);
    }

    private static Class<?> findFactoryClassByName(final String name, final Class<?> type) {
        try {
            return Class.forName(name);
        } catch (final ClassNotFoundException e) {
            throw daggerIntegrationException(format("Unable to find Dagger factory for class '%s' at '%s'", type.getName(), name), e);
        }
    }

    private static Method methodByName(final String name, final Class<?> type) {
        try {
            return type.getMethod(name);
        } catch (final NoSuchMethodException e) {
            throw daggerIntegrationException(format("Cannot find method with name '%s' in class '%s'", name, type.getName()), e);
        }
    }
}
