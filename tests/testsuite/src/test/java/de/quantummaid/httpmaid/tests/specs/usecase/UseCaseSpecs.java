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

import com.google.gson.Gson;
import de.quantummaid.eventmaid.useCases.useCaseAdapter.usecaseInstantiating.ZeroArgumentsConstructorUseCaseInstantiatorException;
import de.quantummaid.httpmaid.tests.givenwhenthen.TestEnvironment;
import de.quantummaid.httpmaid.tests.specs.usecase.usecases.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Map;

import static de.quantummaid.httpmaid.HttpMaid.anHttpMaid;
import static de.quantummaid.httpmaid.events.EventConfigurators.toEnrichTheIntermediateMapUsing;
import static de.quantummaid.httpmaid.events.EventConfigurators.toEnrichTheIntermediateMapWithAllPathParameters;
import static de.quantummaid.httpmaid.exceptions.ExceptionConfigurators.toMapExceptionsByDefaultUsing;
import static de.quantummaid.httpmaid.exceptions.ExceptionConfigurators.toMapExceptionsOfType;
import static de.quantummaid.httpmaid.http.headers.ContentType.json;
import static de.quantummaid.httpmaid.marshalling.MarshallingConfigurators.toMarshallContentType;

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

    @ParameterizedTest
    @MethodSource(TestEnvironment.ALL_ENVIRONMENTS)
    public void useCasesCanReturnStrings(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .get("/", StringReturningUseCase.class)
                        .configured(toMarshallContentType(json(),
                                string -> new Gson().fromJson(string, Map.class),
                                map -> new Gson().toJson(map)))
                        .build()
        )
                .when().aRequestToThePath("/").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theStatusCodeWas(200)
                .theResponseBodyWas("\"the correct response\"");
    }

    @ParameterizedTest
    @MethodSource(TestEnvironment.ALL_ENVIRONMENTS)
    public void useCasesCanReturnInts(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .get("/", IntReturningUseCase.class)
                        .configured(toMarshallContentType(json(),
                                string -> new Gson().fromJson(string, Map.class),
                                map -> new Gson().toJson(map)))
                        .build()
        )
                .when().aRequestToThePath("/").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theStatusCodeWas(200)
                .theResponseBodyWas("\"42\"");
    }

    @ParameterizedTest
    @MethodSource(TestEnvironment.ALL_ENVIRONMENTS)
    public void useCaseCanHaveASingleStringAsParameterWithInlinedRequest(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .post("/", SingleStringParameterUseCase.class)
                        .configured(toMarshallContentType(json(),
                                string -> new Gson().fromJson(string, Object.class),
                                map -> new Gson().toJson(map)))
                        .build()
        )
                .when().aRequestToThePath("/").viaThePostMethod().withTheBody("\"foo\"").withContentType("application/json").isIssued()
                .theStatusCodeWas(200)
                .theResponseBodyWas("\"foo\"");
    }

    @ParameterizedTest
    @MethodSource(TestEnvironment.ALL_ENVIRONMENTS)
    public void useCaseCanHaveASingleStringAsParameterViaPathParameter(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .get("/<parameter>", SingleStringParameterUseCase.class)
                        .configured(toEnrichTheIntermediateMapWithAllPathParameters())
                        .configured(toMarshallContentType(json(),
                                string -> new Gson().fromJson(string, Object.class),
                                map -> new Gson().toJson(map)))
                        .build()
        )
                .when().aRequestToThePath("/foo").viaTheGetMethod().withAnEmptyBody().withContentType("application/json").isIssued()
                .theStatusCodeWas(200)
                .theResponseBodyWas("\"foo\"");
    }

    @ParameterizedTest
    @MethodSource(TestEnvironment.ALL_ENVIRONMENTS)
    public void useCaseCanHaveASingleDtoAsParameterWithInlinedRequest(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .post("/", SingleDtoParameterUseCase.class)
                        .configured(toMarshallContentType(json(),
                                string -> new Gson().fromJson(string, Object.class),
                                map -> new Gson().toJson(map)))
                        .build()
        )
                .when().aRequestToThePath("/").viaThePostMethod().withTheBody("{ \"fieldA\": \"foo\", \"fieldB\": \"bar\" }").withContentType("application/json").isIssued()
                .theStatusCodeWas(200)
                .theResponseBodyWas("\"MyDto(fieldA\\u003dfoo, fieldB\\u003dbar)\"");
    }

    @ParameterizedTest
    @MethodSource(TestEnvironment.ALL_ENVIRONMENTS)
    public void useCaseCanHaveASingleStringAsParameter(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .post("/", SingleStringParameterUseCase.class)
                        .configured(toEnrichTheIntermediateMapUsing((map, request) -> {
                            request.optionalBodyMap().ifPresent(stringObjectMap -> {
                                final Object parameter = stringObjectMap.get("parameter");
                                map.overwriteTopLevel("parameter", parameter);
                            });
                        }))
                        .configured(toMarshallContentType(json(),
                                string -> new Gson().fromJson(string, Object.class),
                                map -> new Gson().toJson(map)))
                        .build()
        )
                .when().aRequestToThePath("/").viaThePostMethod().withTheBody("{ \"parameter\": \"foo\" }").withContentType("application/json").isIssued()
                .theStatusCodeWas(200)
                .theResponseBodyWas("\"foo\"");
    }

    @ParameterizedTest
    @MethodSource(TestEnvironment.ALL_ENVIRONMENTS)
    public void useCaseCanHaveASingleDtoAsParameter(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .post("/", SingleDtoParameterUseCase.class)
                        .configured(toEnrichTheIntermediateMapUsing((map, request) -> {
                            request.optionalBodyMap().ifPresent(stringObjectMap -> {
                                final Object parameter = stringObjectMap.get("parameter");
                                map.overwriteTopLevel("parameter", parameter);
                            });
                        }))
                        .configured(toMarshallContentType(json(),
                                string -> new Gson().fromJson(string, Object.class),
                                map -> new Gson().toJson(map)))
                        .build()
        )
                .when().aRequestToThePath("/").viaThePostMethod().withTheBody("{ \"parameter\": { \"fieldA\": \"foo\", \"fieldB\": \"bar\" } }").withContentType("application/json").isIssued()
                .theStatusCodeWas(200)
                .theResponseBodyWas("\"MyDto(fieldA\\u003dfoo, fieldB\\u003dbar)\"");
    }

    @ParameterizedTest
    @MethodSource(TestEnvironment.ALL_ENVIRONMENTS)
    public void useCaseCanHaveTwoStringsAsParameter(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .post("/", TwoStringsParameterUseCase.class)
                        .configured(toMarshallContentType(json(),
                                string -> new Gson().fromJson(string, Object.class),
                                map -> new Gson().toJson(map)))
                        .build()
        )
                .when().aRequestToThePath("/").viaThePostMethod().withTheBody("{ \"parameter1\": \"foo\", \"parameter2\": \"bar\" }").withContentType("application/json").isIssued()
                .theStatusCodeWas(200)
                .theResponseBodyWas("\"foobar\"");
    }
}