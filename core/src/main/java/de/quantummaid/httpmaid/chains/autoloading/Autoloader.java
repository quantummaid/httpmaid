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

package de.quantummaid.httpmaid.chains.autoloading;

import de.quantummaid.httpmaid.chains.ChainModule;
import de.quantummaid.reflectmaid.resolvedtype.ClassType;
import de.quantummaid.reflectmaid.ReflectMaid;
import de.quantummaid.reflectmaid.resolvedtype.resolver.ResolvedMethod;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;

import static de.quantummaid.httpmaid.chains.autoloading.AutoloadingException.autoloadingException;
import static de.quantummaid.httpmaid.util.Validators.validateNotNullNorEmpty;
import static java.lang.Thread.currentThread;
import static java.util.Optional.empty;
import static java.util.Optional.of;

public final class Autoloader {

    private Autoloader() {
    }

    public static Optional<ChainModule> loadModule(final ReflectMaid reflectMaid, final String fullyQualifiedClassName) {
        validateNotNullNorEmpty(fullyQualifiedClassName, "fullyQualifiedClassName");
        return loadClass(fullyQualifiedClassName).map(clazz -> {
            final ResolvedMethod staticInitializer = findStaticInitializer(reflectMaid, clazz);
            return invoke(staticInitializer);
        });
    }

    @SuppressWarnings("unchecked")
    private static Optional<Class<? extends ChainModule>> loadClass(final String fullyQualifiedClassName) {
        final ClassLoader classLoader = currentThread().getContextClassLoader();
        try {
            final Class<? extends ChainModule> clazz =
                    (Class<? extends ChainModule>) classLoader.loadClass(fullyQualifiedClassName);
            return of(clazz);
        } catch (final ClassNotFoundException e) {
            return empty();
        }
    }

    private static ResolvedMethod findStaticInitializer(final ReflectMaid reflectMaid,
                                                        final Class<? extends ChainModule> clazz) {
        final ClassType classType = (ClassType) reflectMaid.resolve(clazz);
        return classType.methods().stream()
                .filter(ResolvedMethod::isPublic)
                .filter(ResolvedMethod::isStatic)
                .filter(method -> method.returnType()
                        .map(classType::equals)
                        .orElse(false))
                .filter(method -> method.getParameters().isEmpty())
                .findFirst()
                .orElseThrow();
    }

    private static ChainModule invoke(final ResolvedMethod staticInitializer) {
        final Method method = staticInitializer.getMethod();
        try {
            return (ChainModule) method.invoke(null);
        } catch (final IllegalAccessException | InvocationTargetException e) {
            throw autoloadingException(e);
        }
    }
}
