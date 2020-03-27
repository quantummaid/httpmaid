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

package de.quantummaid.httpmaid.tests.specs.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import de.quantummaid.httpmaid.tests.givenwhenthen.TestEnvironment;
import de.quantummaid.httpmaid.tests.specs.guice.domain.AnnotatedUseCase;
import de.quantummaid.httpmaid.tests.specs.guice.domain.HardToInstantiateComponent;
import de.quantummaid.httpmaid.tests.specs.guice.domain.HardToInstantiateUseCase;
import de.quantummaid.httpmaid.tests.specs.guice.domain.SingleConstructorUseCase;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static de.quantummaid.httpmaid.HttpMaid.anHttpMaid;
import static de.quantummaid.httpmaid.guice.GuiceConfigurators.toCreateUseCaseInstancesUsingGuice;
import static de.quantummaid.httpmaid.tests.specs.guice.domain.HardToInstantiateComponent.HARD_TO_INSTANTIATE_COMPONENT;

public final class GuiceSpecs {

    @ParameterizedTest
    @MethodSource(TestEnvironment.ALL_ENVIRONMENTS)
    public void guiceIntegrationCanWorkWithoutSpecifyingAnInjector(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .get("/", SingleConstructorUseCase.class)
                        .configured(toCreateUseCaseInstancesUsingGuice())
                        .build()
        )
                .when().aRequestToThePath("/").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theResponseBodyWas("\"the correct component\"");
    }

    @ParameterizedTest
    @MethodSource(TestEnvironment.ALL_ENVIRONMENTS)
    public void guiceIntegrationFailsPerDefaultForMoreThanOneConstructor(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                () -> anHttpMaid()
                        .get("/", AnnotatedUseCase.class)
                        .configured(toCreateUseCaseInstancesUsingGuice())
                        .build()
        )
                .when().httpMaidIsInitialized()
                .anExceptionHasBeenThrownDuringInitializationWithAMessageContaining("Can only bind classes that have exactly one public constructor. " +
                        "Class 'de.quantummaid.httpmaid.tests.specs.guice.domain.AnnotatedUseCase' has the following constructors:");
    }

    @ParameterizedTest
    @MethodSource(TestEnvironment.ALL_ENVIRONMENTS)
    public void guiceIntegrationCanGetACustomInjector(final TestEnvironment testEnvironment) {
        final Injector injector = Guice.createInjector();
        testEnvironment.given(
                anHttpMaid()
                        .get("/", AnnotatedUseCase.class)
                        .configured(toCreateUseCaseInstancesUsingGuice(injector))
                        .build()
        )
                .when().aRequestToThePath("/").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theResponseBodyWas("\"the correct component\"");
    }

    @ParameterizedTest
    @MethodSource(TestEnvironment.ALL_ENVIRONMENTS)
    public void guiceIntegrationCanSpecifyModules(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .get("/", HardToInstantiateUseCase.class)
                        .configured(toCreateUseCaseInstancesUsingGuice(new AbstractModule() {
                            @Override
                            protected void configure() {
                                bind(HardToInstantiateComponent.class).toProvider(() -> HARD_TO_INSTANTIATE_COMPONENT);
                            }
                        }))
                        .build()
        )
                .when().aRequestToThePath("/").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theResponseBodyWas("\"the hard-to-instantiate component\"");
    }
}
