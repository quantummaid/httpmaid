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
import de.quantummaid.httpmaid.chains.ChainExtender;
import de.quantummaid.httpmaid.chains.ChainModule;
import de.quantummaid.httpmaid.chains.DependencyRegistry;
import de.quantummaid.httpmaid.usecases.UseCasesModule;
import de.quantummaid.httpmaid.usecases.instantiation.UseCaseInstantiator;
import de.quantummaid.httpmaid.usecases.instantiation.UseCaseInstantiatorFactory;
import de.quantummaid.httpmaid.util.Validators;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import static de.quantummaid.httpmaid.guice.GuiceUseCaseInstantiator.guiceUseCaseInstantiator;
import static de.quantummaid.httpmaid.guice.GuiceUseCaseInstantiatorFactory.guiceUseCaseInstantiatorFactory;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class GuiceModule implements ChainModule {
    private UseCaseInstantiatorFactory factory = guiceUseCaseInstantiatorFactory();

    public static GuiceModule guiceModule() {
        return new GuiceModule();
    }

    public void setInjector(final Injector injector) {
        Validators.validateNotNull(injector, "injector");
        final UseCaseInstantiator useCaseInstantiator = guiceUseCaseInstantiator(injector);
        this.factory = requiredTypes -> useCaseInstantiator;
    }

    @Override
    public void configure(final DependencyRegistry dependencyRegistry) {
        final UseCasesModule useCasesModule = dependencyRegistry.getDependency(UseCasesModule.class);
        useCasesModule.setUseCaseInstantiatorFactory(factory);
    }

    @Override
    public void register(final ChainExtender extender) {
    }
}
