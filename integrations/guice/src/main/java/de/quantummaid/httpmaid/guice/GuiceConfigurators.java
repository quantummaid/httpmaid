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

package de.quantummaid.httpmaid.guice;

import com.google.inject.Injector;
import com.google.inject.Module;
import de.quantummaid.httpmaid.chains.Configurator;
import de.quantummaid.httpmaid.usecases.UseCasesModule;
import de.quantummaid.httpmaid.usecases.instantiation.UseCaseInstantiatorFactory;

import java.util.List;

import static de.quantummaid.httpmaid.chains.Configurator.configuratorForType;
import static de.quantummaid.httpmaid.guice.factories.GuiceUseCaseInstantiatorFactory.guiceUseCaseInstantiatorFactory;
import static de.quantummaid.httpmaid.guice.factories.ProvidedInjectorInstantiatorFactory.providedInjectorInstantiatorFactory;
import static de.quantummaid.httpmaid.util.Validators.validateNotNull;
import static java.util.Arrays.asList;

public final class GuiceConfigurators {

    private GuiceConfigurators() {
    }

    public static Configurator toCreateUseCaseInstancesUsingGuice() {
        final UseCaseInstantiatorFactory factory = guiceUseCaseInstantiatorFactory();
        return guiceConfigurator(factory);
    }

    public static Configurator toCreateUseCaseInstancesUsingGuice(final Module... modules) {
        final List<Module> modulesList = asList(modules);
        final UseCaseInstantiatorFactory factory = guiceUseCaseInstantiatorFactory(modulesList);
        return guiceConfigurator(factory);
    }

    public static Configurator toCreateUseCaseInstancesUsingGuice(final Injector injector) {
        validateNotNull(injector, "injector");
        final UseCaseInstantiatorFactory factory = providedInjectorInstantiatorFactory(injector);
        return guiceConfigurator(factory);
    }

    private static Configurator guiceConfigurator(final UseCaseInstantiatorFactory factory) {
        return configuratorForType(UseCasesModule.class, useCasesModule ->
                useCasesModule.setUseCaseInstantiatorFactory(factory));
    }
}
