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

import de.quantummaid.httpmaid.chains.Configurator;
import de.quantummaid.injectmaid.api.InjectorConfiguration;
import de.quantummaid.mapmaid.builder.recipes.Recipe;

import static de.quantummaid.httpmaid.chains.Configurator.configuratorForType;

public final class UseCaseConfigurators {

    private UseCaseConfigurators() {
    }

    public static Configurator withMapperConfiguration(final Recipe recipe) {
        return dependencyRegistry -> {
            final UseCasesModule useCasesModule = dependencyRegistry.getDependency(UseCasesModule.class);
            useCasesModule.addMapperConfiguration(recipe);
        };
    }

    public static Configurator toSetStatusCodeOnMapMaidValidationErrorsTo(final int statusCode) {
        return configuratorForType(UseCasesModule.class, module -> module.setValidationErrorStatusCode(statusCode));
    }

    public static Configurator toNotCreateAnAutomaticResponseForMapMaidValidationErrors() {
        return configuratorForType(UseCasesModule.class, UseCasesModule::doNotAddAggregatedExceptionHandler);
    }

    public static Configurator withGlobalScopedDependencies(final InjectorConfiguration configuration) {
        return dependencyRegistry -> {
            final UseCasesModule useCasesModule = dependencyRegistry.getDependency(UseCasesModule.class);
            useCasesModule.addGlobalScopedInjectorConfiguration(configuration);
        };
    }

    public static Configurator withRequestScopedDependencies(final InjectorConfiguration configuration) {
        return dependencyRegistry -> {
            final UseCasesModule useCasesModule = dependencyRegistry.getDependency(UseCasesModule.class);
            useCasesModule.addRequestScopedInjectorConfiguration(configuration);
        };
    }
}
