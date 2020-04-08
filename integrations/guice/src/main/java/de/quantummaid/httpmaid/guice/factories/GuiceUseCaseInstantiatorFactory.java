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

package de.quantummaid.httpmaid.guice.factories;

import com.google.inject.Injector;
import com.google.inject.Module;
import de.quantummaid.httpmaid.usecases.instantiation.UseCaseInstantiator;
import de.quantummaid.httpmaid.usecases.instantiation.UseCaseInstantiatorFactory;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.List;

import static com.google.inject.Guice.createInjector;
import static de.quantummaid.httpmaid.guice.GuiceUseCaseInstantiator.guiceUseCaseInstantiator;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class GuiceUseCaseInstantiatorFactory implements UseCaseInstantiatorFactory {
    private final List<Module> configuredModules;

    public static UseCaseInstantiatorFactory guiceUseCaseInstantiatorFactory() {
        return new GuiceUseCaseInstantiatorFactory(emptyList());
    }

    public static UseCaseInstantiatorFactory guiceUseCaseInstantiatorFactory(final List<Module> configuredModules) {
        return new GuiceUseCaseInstantiatorFactory(configuredModules);
    }

    @Override
    public UseCaseInstantiator createInstantiator(final List<Class<?>> requiredTypes) {
        final List<Module> modules = requiredTypes.stream()
                .map(SinglePublicConstructorModule::singlePublicConstructorModule)
                .collect(toList());
        modules.addAll(this.configuredModules);
        final Injector injector = createInjector(modules);
        return guiceUseCaseInstantiator(injector);
    }
}