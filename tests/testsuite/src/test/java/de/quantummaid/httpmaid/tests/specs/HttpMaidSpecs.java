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

package de.quantummaid.httpmaid.tests.specs;

import de.quantummaid.httpmaid.tests.givenwhenthen.TestEnvironment;
import de.quantummaid.httpmaid.tests.usecases.mapmaid.MapMaidUseCase;
import de.quantummaid.httpmaid.tests.usecases.twoparameters.TwoParametersUseCase;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static de.quantummaid.httpmaid.HttpMaid.anHttpMaid;
import static de.quantummaid.httpmaid.events.EventConfigurators.mappingHeader;
import static de.quantummaid.httpmaid.events.EventConfigurators.mappingPathParameter;
import static de.quantummaid.httpmaid.tests.HttpMaidTestConfigurations.theHttpMaidInstanceUsedForTesting;

public final class HttpMaidSpecs {

    @ParameterizedTest
    @MethodSource(TestEnvironment.ALL_ENVIRONMENTS)
    public void testGetRequest(final TestEnvironment testEnvironment) {
        testEnvironment.given(theHttpMaidInstanceUsedForTesting())
                .when().aRequestToThePath("/test").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theStatusCodeWas(200)
                .theResponseContentTypeWas("application/json")
                .theResponseBodyWas("\"foo\"");
    }

    @ParameterizedTest
    @MethodSource(TestEnvironment.ALL_ENVIRONMENTS)
    public void testPostRequest(final TestEnvironment testEnvironment) {
        testEnvironment.given(theHttpMaidInstanceUsedForTesting())
                .when().aRequestToThePath("/test").viaThePostMethod().withAnEmptyBody().isIssued()
                .theStatusCodeWas(200)
                .theResponseContentTypeWas("application/json")
                .theResponseBodyWas("\"foo\"");
    }

    @ParameterizedTest
    @MethodSource(TestEnvironment.ALL_ENVIRONMENTS)
    public void testPutRequest(final TestEnvironment testEnvironment) {
        testEnvironment.given(theHttpMaidInstanceUsedForTesting())
                .when().aRequestToThePath("/test").viaThePutMethod().withAnEmptyBody().isIssued()
                .theStatusCodeWas(200)
                .theResponseContentTypeWas("application/json")
                .theResponseBodyWas("\"foo\"");
    }

    @ParameterizedTest
    @MethodSource(TestEnvironment.ALL_ENVIRONMENTS)
    public void testDeleteRequest(final TestEnvironment testEnvironment) {
        testEnvironment.given(theHttpMaidInstanceUsedForTesting())
                .when().aRequestToThePath("/test").viaTheDeleteMethod().withAnEmptyBody().isIssued()
                .theStatusCodeWas(200)
                .theResponseContentTypeWas("application/json")
                .theResponseBodyWas("\"foo\"");
    }

    @ParameterizedTest
    @MethodSource(TestEnvironment.ALL_ENVIRONMENTS)
    public void testWildcardRouteWithEmptyMiddleWildcard(final TestEnvironment testEnvironment) {
        testEnvironment.given(theHttpMaidInstanceUsedForTesting())
                .when().aRequestToThePath("/wild/card").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theStatusCodeWas(405)
                .theResponseBodyWas("No use case found.");
    }

    @ParameterizedTest
    @MethodSource(TestEnvironment.ALL_ENVIRONMENTS)
    public void testUseCaseNotFoundExceptionHandler(final TestEnvironment testEnvironment) {
        testEnvironment.given(theHttpMaidInstanceUsedForTesting())
                .when().aRequestToThePath("/this_has_no_usecase").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theStatusCodeWas(405)
                .theResponseBodyWas("No use case found.");
    }

    @ParameterizedTest
    @MethodSource(TestEnvironment.ALL_ENVIRONMENTS)
    public void testMapMaidOnlyWithInjectionAndWithoutBody(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .get("/mapmaid/<value1>", MapMaidUseCase.class,
                                mappingPathParameter("value1", "dataTransferObject.value1"),
                                mappingHeader("value2", "dataTransferObject.value2"),
                                mappingHeader("value3", "dataTransferObject.value3"),
                                mappingHeader("value4", "dataTransferObject.value4"))
                        .build()
        )
                .when().aRequestToThePath("/mapmaid/derp").viaTheGetMethod().withAnEmptyBody()
                .withTheHeader("value2", "merp").withTheHeader("value3", "herp").withTheHeader("value4", "qerp").isIssued()
                .theStatusCodeWas(200)
                .theResponseContentTypeWas("application/json")
                .theJsonResponseEquals("" +
                        "{" +
                        "   value1: derp," +
                        "   value2: merp," +
                        "   value3: herp," +
                        "   value4: qerp" +
                        "}"
                );
    }

    @ParameterizedTest
    @MethodSource(TestEnvironment.ALL_ENVIRONMENTS)
    public void testTwoUseCaseParameters(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                () -> anHttpMaid()
                        .get("/twoparameters", TwoParametersUseCase.class, mappingHeader("param1"), mappingHeader("param2"))
                        .build()
        )
                .when().aRequestToThePath("/twoparameters").viaTheGetMethod().withAnEmptyBody().withTheHeader("param1", "Hello").withTheHeader("param2", "World").isIssued()
                .theStatusCodeWas(200)
                .theResponseContentTypeWas("application/json")
                .theResponseBodyWas("\"Hello World\"");
    }

    @ParameterizedTest
    @MethodSource(TestEnvironment.ALL_ENVIRONMENTS)
    public void testVoidUseCase(final TestEnvironment testEnvironment) {
        testEnvironment.given(theHttpMaidInstanceUsedForTesting())
                .when().aRequestToThePath("/void").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theStatusCodeWas(200)
                .theResponseContentTypeWas("application/json");
    }

    @ParameterizedTest
    @MethodSource(TestEnvironment.ALL_ENVIRONMENTS)
    public void testContentTypeCanContainParameters(final TestEnvironment testEnvironment) {
        testEnvironment.given(theHttpMaidInstanceUsedForTesting())
                .when().aRequestToThePath("/void").viaTheGetMethod().withAnEmptyBody().withContentType("application/json; charset=iso-8859-1").isIssued()
                .theStatusCodeWas(200)
                .theResponseContentTypeWas("application/json");
    }
}
