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

package de.quantummaid.httpmaid.dagger;

import de.quantummaid.httpmaid.chains.Configurator;
import de.quantummaid.httpmaid.dagger.factoryfinding.DaggerFactoryFinder;
import de.quantummaid.httpmaid.dagger.factoryfinding.NamePattern;
import de.quantummaid.httpmaid.usecases.UseCasesModule;
import de.quantummaid.httpmaid.usecases.instantiation.UseCaseInstantiator;
import de.quantummaid.httpmaid.usecases.instantiation.UseCaseInstantiatorFactory;

import static de.quantummaid.httpmaid.chains.Configurator.configuratorForType;
import static de.quantummaid.httpmaid.dagger.DaggerInjector.daggerInjector;
import static de.quantummaid.httpmaid.dagger.factoryfinding.NamePattern.defaultFactoryClassPattern;
import static de.quantummaid.httpmaid.dagger.factoryfinding.NamePattern.defaultFactoryMethodPattern;
import static de.quantummaid.httpmaid.dagger.factoryfinding.PatternBasedFactoryFinder.patternBasedFactoryFinder;

public final class DaggerConfigurators {
    private static final String DEFAULT_FACTORY_CONSTRUCTOR_NAME = "create";

    private DaggerConfigurators() {
    }

    public static Configurator toCreateUseCaseInstancesUsingDagger() {
        return toCreateUseCaseInstancesUsingDagger(
                defaultFactoryClassPattern(),
                DEFAULT_FACTORY_CONSTRUCTOR_NAME,
                defaultFactoryMethodPattern()
        );
    }

    public static Configurator toCreateUseCaseInstancesUsingDagger(final NamePattern factoryClassNamePattern,
                                                                   final String factoryConstructorName,
                                                                   final NamePattern factoryMethodNamePattern) {
        final DaggerFactoryFinder factoryFinder = patternBasedFactoryFinder(
                factoryClassNamePattern,
                factoryConstructorName,
                factoryMethodNamePattern
        );
        return toCreateUseCaseInstancesUsingDagger(factoryFinder);
    }

    public static Configurator toCreateUseCaseInstancesUsingDagger(final DaggerFactoryFinder factoryFinder) {
        return configuratorForType(UseCasesModule.class, useCasesModule -> {
            final UseCaseInstantiator useCaseInstantiator = daggerInjector(factoryFinder);
            final UseCaseInstantiatorFactory factory = useCases -> useCaseInstantiator;
            useCasesModule.setUseCaseInstantiatorFactory(factory);
        });
    }
}
