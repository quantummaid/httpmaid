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

import de.quantummaid.httpmaid.exceptions.ExceptionConfigurators;
import de.quantummaid.httpmaid.handler.NoHandlerFoundException;
import de.quantummaid.httpmaid.tests.givenwhenthen.TestEnvironment;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static de.quantummaid.httpmaid.HttpMaid.anHttpMaid;

public final class PathSpecs {

    @ParameterizedTest
    @MethodSource(TestEnvironment.ALL_ENVIRONMENTS)
    public void wildcardPathMatchesSingleElement(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .get("/*", (request, response) -> response.setBody("handler has been called"))
                        .build()
        )
                .when().aRequestToThePath("/aaa").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theResponseBodyWas("handler has been called");
    }

    @ParameterizedTest
    @MethodSource(TestEnvironment.ALL_ENVIRONMENTS)
    public void wildcardPathMatchesZeroElements(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .get("/*", (request, response) -> response.setBody("handler has been called"))
                        .build()
        )
                .when().aRequestToThePath("/").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theResponseBodyWas("handler has been called");
    }

    @ParameterizedTest
    @MethodSource(TestEnvironment.ALL_ENVIRONMENTS)
    public void wildcardPathMatchesMultipleElements(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .get("/*", (request, response) -> response.setBody("handler has been called"))
                        .build()
        )
                .when().aRequestToThePath("/aaa/bbb/ccc").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theResponseBodyWas("handler has been called");
    }

    @ParameterizedTest
    @MethodSource(TestEnvironment.ALL_ENVIRONMENTS)
    public void multipleWildcardsMatchIfThePathHasTheIntermediateElements(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .get("/*/a/*/b/*/c", (request, response) -> response.setBody("handler has been called"))
                        .build()
        )
                .when().aRequestToThePath("/x/x/x/a/y/y/y/b/z/z/z/c").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theResponseBodyWas("handler has been called");
    }

    @ParameterizedTest
    @MethodSource(TestEnvironment.ALL_ENVIRONMENTS)
    public void multipleWildcardsMatchNotIfThePathDoesNotHaveTheIntermediateElements(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .get("/*/a/*/b/*/c", (request, response) -> response.setBody("handler has been called"))
                        .configured(ExceptionConfigurators.toMapExceptionsOfType(NoHandlerFoundException.class, (exception, response) -> response.setBody("no handler")))
                        .build()
        )
                .when().aRequestToThePath("/x/x/x/a/y/y/y/z/z/z/c").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theResponseBodyWas("no handler");
    }
}