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

package de.quantummaid.httpmaid.tests.specs.usecase;

import de.quantummaid.httpmaid.tests.givenwhenthen.TestEnvironment;
import de.quantummaid.httpmaid.tests.specs.usecase.specialusecases.usecases.UseCaseThatIsAnAbstractClass;
import de.quantummaid.httpmaid.tests.specs.usecase.specialusecases.usecases.UseCaseThatIsAnInterface;
import de.quantummaid.httpmaid.tests.specs.usecase.usecases.FailInInitializerUseCase;
import de.quantummaid.injectmaid.InjectMaidException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static de.quantummaid.httpmaid.HttpMaid.anHttpMaid;
import static de.quantummaid.httpmaid.chains.Configurator.toUseModules;
import static de.quantummaid.httpmaid.exceptions.ExceptionConfigurators.toMapExceptionsByDefaultUsing;
import static de.quantummaid.httpmaid.exceptions.ExceptionConfigurators.toMapExceptionsOfType;
import static de.quantummaid.httpmaid.tests.givenwhenthen.TestEnvironments.ALL_ENVIRONMENTS;
import static de.quantummaid.httpmaid.usecases.UseCasesModule.useCasesModule;

public final class InstantiationSpecs {

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void exceptionInInitializerIsThrownOnStartup(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                () -> anHttpMaid()
                        .get("/", FailInInitializerUseCase.class)
                        .disableAutodectectionOfModules()
                        .configured(toUseModules(useCasesModule()))
                        .build()
        )
                .when().httpMaidIsInitialized()
                .anExceptionHasBeenThrownDuringInitializationWithAMessageContaining("Exception during instantiation of 'FailInInitializerUseCase' " +
                        "using constructor 'public de.quantummaid.httpmaid.tests.specs.usecase.usecases.FailInInitializerUseCase()'");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void exceptionInInitializerCanBeCaughtInSpecializedHandler(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .get("/", FailInInitializerUseCase.class)
                        .configured(toMapExceptionsOfType(InjectMaidException.class,
                                (exception, request, response) -> {
                                    response.setBody("The correct exception has been thrown");
                                    response.setStatus(505);
                                }))
                        .configured(toMapExceptionsByDefaultUsing((exception, request, response) -> {
                            response.setBody("The incorrect exception has been thrown");
                            response.setStatus(501);
                        }))
                        .disableStartupChecks()
                        .disableAutodectectionOfModules()
                        .configured(toUseModules(useCasesModule()))
                        .build()
        )
                .when().aRequestToThePath("/").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theStatusCodeWas(505)
                .theResponseBodyWas("The correct exception has been thrown");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void exceptionInInitializerCanBeCaughtInDefaultHandler(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .get("/", FailInInitializerUseCase.class)
                        .disableStartupChecks()
                        .build()
        )
                .when().aRequestToThePath("/").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theStatusCodeWas(500)
                .theResponseBodyWas("");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void defaultInstantiatorFailsForInterfacesOnRuntime(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                () -> anHttpMaid()
                        .get("/", UseCaseThatIsAnInterface.class)
                        .disableAutodectectionOfModules()
                        .configured(toUseModules(useCasesModule()))
                        .build()
        )
                .when().httpMaidIsInitialized()
                .anExceptionHasBeenThrownDuringInitializationWithAMessageContaining("de.quantummaid.httpmaid.tests.specs.usecase.specialusecases.usecases.UseCaseThatIsAnInterface:\n" +
                        "unable to detect registered:");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void defaultInstantiatorFailsForInterfacesOnStartup(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                () -> anHttpMaid()
                        .get("/", UseCaseThatIsAnInterface.class)
                        .disableAutodectectionOfModules()
                        .configured(toUseModules(useCasesModule()))
                        .build()
        )
                .when().httpMaidIsInitialized()
                .anExceptionHasBeenThrownDuringInitializationWithAMessageContaining("de.quantummaid.httpmaid.tests.specs.usecase.specialusecases.usecases.UseCaseThatIsAnInterface:\n" +
                        "unable to detect registered:");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void defaultInstantiatorFailsForAbstractClassOnStartup(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                () -> anHttpMaid()
                        .get("/", UseCaseThatIsAnAbstractClass.class)
                        .disableAutodectectionOfModules()
                        .disableStartupChecks()
                        .configured(toUseModules(useCasesModule()))
                        .build()
        )
                .when().httpMaidIsInitialized()
                .anExceptionHasBeenThrownDuringInitializationWithAMessageContaining("unable to detect");
    }
}
