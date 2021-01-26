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
import de.quantummaid.httpmaid.tests.specs.usecase.usecases.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static de.quantummaid.httpmaid.HttpMaid.anHttpMaid;
import static de.quantummaid.httpmaid.events.EventConfigurators.mappingHeader;
import static de.quantummaid.httpmaid.events.EventConfigurators.mappingPathParameter;
import static de.quantummaid.httpmaid.exceptions.ExceptionConfigurators.toMapExceptionsByDefaultUsing;
import static de.quantummaid.httpmaid.exceptions.ExceptionConfigurators.toMapExceptionsOfType;
import static de.quantummaid.httpmaid.tests.givenwhenthen.TestEnvironments.ALL_ENVIRONMENTS;

public final class UseCaseSpecs {

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void testGetRequest(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .get("/test", StringReturningUseCase.class)
                        .build()
        )
                .when().aRequestToThePath("/test").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theStatusCodeWas(200)
                .theResponseContentTypeWas("application/json")
                .theResponseBodyWas("\"the correct response\"");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void testPostRequest(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .post("/test", StringReturningUseCase.class)
                        .build()
        )
                .when().aRequestToThePath("/test").viaThePostMethod().withAnEmptyBody().isIssued()
                .theStatusCodeWas(200)
                .theResponseContentTypeWas("application/json")
                .theResponseBodyWas("\"the correct response\"");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void testPutRequest(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .put("/test", StringReturningUseCase.class)
                        .build()
        )
                .when().aRequestToThePath("/test").viaThePutMethod().withAnEmptyBody().isIssued()
                .theStatusCodeWas(200)
                .theResponseContentTypeWas("application/json")
                .theResponseBodyWas("\"the correct response\"");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void testDeleteRequest(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .delete("/test", StringReturningUseCase.class)
                        .build()
        )
                .when().aRequestToThePath("/test").viaTheDeleteMethod().withAnEmptyBody().isIssued()
                .theStatusCodeWas(200)
                .theResponseContentTypeWas("application/json")
                .theResponseBodyWas("\"the correct response\"");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void testTwoUseCaseParameters(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                () -> anHttpMaid()
                        .get("/twoparameters", TwoStringsParameterUseCase.class, mappingHeader("parameter1"), mappingHeader("parameter2"))
                        .build()
        )
                .when().aRequestToThePath("/twoparameters").viaTheGetMethod().withAnEmptyBody().withTheHeader("parameter1", "Hello").withTheHeader("parameter2", "World").isIssued()
                .theStatusCodeWas(200)
                .theResponseContentTypeWas("application/json")
                .theResponseBodyWas("\"HelloWorld\"");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void testVoidUseCase(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .get("/void", VoidUseCase.class)
                        .build()
        )
                .when().aRequestToThePath("/void").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theStatusCodeWas(200)
                .theResponseContentTypeWas("application/json");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void aUseCaseWithNoParametersAndVoidReturnTypeCanBeInvokedWithoutConfiguringAnySerializers(final TestEnvironment testEnvironment) {
        testEnvironment.given(anHttpMaid().get("/", VoidUseCase.class).build())
                .when().aRequestToThePath("/").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theStatusCodeWas(200)
                .theResponseBodyWas("{}");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void checkedExceptionInUseCaseCanBeCaughtInSpecializedHandler(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .get("/", ThrowCheckedExceptionUseCase.class)
                        .configured(toMapExceptionsOfType(SomeCheckedException.class, (exception, request, response) -> {
                            response.setBody("The correct exception has been thrown");
                            response.setStatus(505);
                        }))
                        .configured(toMapExceptionsByDefaultUsing((exception, request, response) -> {
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
    @MethodSource(ALL_ENVIRONMENTS)
    public void useCasesCanReturnStrings(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .get("/", StringReturningUseCase.class)
                        .build()
        )
                .when().aRequestToThePath("/").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theStatusCodeWas(200)
                .theResponseBodyWas("\"the correct response\"");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void useCasesCanReturnInts(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .get("/", IntReturningUseCase.class)
                        .build()
        )
                .when().aRequestToThePath("/").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theStatusCodeWas(200)
                .theResponseBodyWas("42");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void useCaseCanHaveASingleStringAsParameterWithInlinedRequest(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .post("/", SingleStringParameterUseCase.class)
                        .build()
        )
                .when().aRequestToThePath("/").viaThePostMethod().withTheBody("\"foo\"").withContentType("application/json").isIssued()
                .theStatusCodeWas(200)
                .theResponseBodyWas("\"foo\"");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void useCaseCanHaveASingleStringAsParameterViaPathParameter(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .get("/<parameter>", SingleStringParameterUseCase.class, mappingPathParameter("parameter"))
                        .build()
        )
                .when().aRequestToThePath("/foo").viaTheGetMethod().withAnEmptyBody().withContentType("application/json").isIssued()
                .theStatusCodeWas(200)
                .theResponseBodyWas("\"foo\"");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void useCaseCanHaveASingleDtoAsParameterWithInlinedRequest(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .post("/", SingleDtoParameterUseCase.class)
                        .build()
        )
                .when().aRequestToThePath("/").viaThePostMethod().withTheBody("{ \"fieldA\": \"foo\", \"fieldB\": \"bar\" }").withContentType("application/json").isIssued()
                .theStatusCodeWas(200)
                .theResponseBodyWas("\"MyDto(fieldA=foo, fieldB=bar)\"");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void useCaseCanHaveTwoStringsAsParameter(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .post("/", TwoStringsParameterUseCase.class)
                        .build()
        )
                .when().aRequestToThePath("/").viaThePostMethod().withTheBody("{ \"parameter1\": \"foo\", \"parameter2\": \"bar\" }").withContentType("application/json").isIssued()
                .theStatusCodeWas(200)
                .theResponseBodyWas("\"foobar\"");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void useCaseWithSameDtoInRequestAndResponse(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .post("/", UseCaseWithSameDtoInRequestAndResponse.class)
                        .build()
        )
                .when().aRequestToThePath("/").viaThePostMethod()
                .withTheBody("{ \"id\": \"abc\", \"dto\": { \"fieldA\": \"foo\", \"fieldB\": \"bar\" } }")
                .withContentType("application/json").isIssued()
                .theStatusCodeWas(200)
                .theJsonResponseEquals("{\"value\":{\"fieldB\":\"bar\",\"fieldA\":\"foo\"},\"id\":\"abc\"}");
    }
}
