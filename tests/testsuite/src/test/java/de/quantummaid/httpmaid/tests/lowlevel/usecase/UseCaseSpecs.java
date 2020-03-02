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

package de.quantummaid.httpmaid.tests.lowlevel.usecase;

import de.quantummaid.httpmaid.tests.givenwhenthen.TestEnvironment;
import de.quantummaid.httpmaid.tests.lowlevel.usecase.usecases.FailInInitializerUseCase;
import de.quantummaid.httpmaid.tests.lowlevel.usecase.usecases.SomeCheckedException;
import de.quantummaid.httpmaid.tests.lowlevel.usecase.usecases.ThrowCheckedExceptionUseCase;
import de.quantummaid.httpmaid.tests.lowlevel.usecase.usecases.VoidUseCase;
import de.quantummaid.eventmaid.useCases.useCaseAdapter.usecaseInstantiating.ZeroArgumentsConstructorUseCaseInstantiatorException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static de.quantummaid.httpmaid.HttpMaid.anHttpMaid;
import static de.quantummaid.httpmaid.exceptions.ExceptionConfigurators.toMapExceptionsByDefaultUsing;
import static de.quantummaid.httpmaid.exceptions.ExceptionConfigurators.toMapExceptionsOfType;

public final class UseCaseSpecs {

    @ParameterizedTest
    @MethodSource(TestEnvironment.ALL_ENVIRONMENTS)
    public void aUseCaseWithNoParametersAndVoidReturnTypeCanBeInvokedWithoutConfiguringAnySerializers(final TestEnvironment testEnvironment) {
        testEnvironment.given(anHttpMaid().get("/", VoidUseCase.class).build())
                .when().aRequestToThePath("/").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theStatusCodeWas(200)
                .theResponseBodyWas("");
    }

    @ParameterizedTest
    @MethodSource(TestEnvironment.ALL_ENVIRONMENTS)
    public void exceptionInInitializerCanBeCaughtInSpecializedHandler(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .get("/", FailInInitializerUseCase.class)
                        .configured(toMapExceptionsOfType(ZeroArgumentsConstructorUseCaseInstantiatorException.class, (exception, response) -> {
                            response.setBody("The correct exception has been thrown");
                            response.setStatus(505);
                        }))
                        .configured(toMapExceptionsByDefaultUsing((exception, response) -> {
                            response.setBody("The incorrect exception has been thrown");
                            response.setStatus(501);
                        }))
                        .build()
        )
                .when().aRequestToThePath("/").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theStatusCodeWas(505)
                .theResponseBodyWas("The correct exception has been thrown");
    }

    @ParameterizedTest
    @MethodSource(TestEnvironment.ALL_ENVIRONMENTS)
    public void exceptionInInitializerCanBeCaughtInDefaultHandler(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .get("/", FailInInitializerUseCase.class)
                        .build()
        )
                .when().aRequestToThePath("/").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theStatusCodeWas(500)
                .theResponseBodyWas("");
    }

    @ParameterizedTest
    @MethodSource(TestEnvironment.ALL_ENVIRONMENTS)
    public void checkedExceptionInUseCaseCanBeCaughtInSpecializedHandler(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .get("/", ThrowCheckedExceptionUseCase.class)
                        .configured(toMapExceptionsOfType(SomeCheckedException.class, (exception, response) -> {
                            response.setBody("The correct exception has been thrown");
                            response.setStatus(505);
                        }))
                        .configured(toMapExceptionsByDefaultUsing((exception, response) -> {
                            response.setBody("The incorrect exception has been thrown");
                            response.setStatus(501);
                        }))
                        .build()
        )
                .when().aRequestToThePath("/").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theStatusCodeWas(505)
                .theResponseBodyWas("The correct exception has been thrown");
    }
}
