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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.concurrent.TimeUnit;

import static de.quantummaid.httpmaid.HttpMaid.anHttpMaid;
import static de.quantummaid.httpmaid.cors.CorsConfigurators.toActivateCORSWithoutValidatingTheOrigin;
import static de.quantummaid.httpmaid.exceptions.ExceptionConfigurators.toMapExceptionsByDefaultUsing;
import static de.quantummaid.httpmaid.exceptions.ExceptionConfigurators.toMapExceptionsOfType;
import static de.quantummaid.httpmaid.http.HttpRequestMethod.*;
import static de.quantummaid.httpmaid.tests.givenwhenthen.TestEnvironments.ALL_ENVIRONMENTS;

public final class CorsSpecs {

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void corsHeadersAreSetForNormalRequests(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid().get("/test", (request, response) -> response.setBody("qwer"))
                        .configured(toActivateCORSWithoutValidatingTheOrigin()
                                .exposingTheResponseHeaders("Some-Header", "Another-Header", "Yet-Another-Header")
                                .allowingCredentials())
                        .build()
        )
                .when().aRequestToThePath("/test").viaTheGetMethod().withAnEmptyBody().withTheHeader("Origin", "localhost").isIssued()
                .theResponseBodyWas("qwer")
                .theReponseContainsTheHeader("Access-Control-Allow-Origin", "localhost")
                .theReponseContainsTheHeader("Access-Control-Allow-Credentials", "true")
                .theReponseContainsTheHeaderRawValue("Access-Control-Expose-Headers", "some-header, another-header, yet-another-header");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void corsHeadersAreSetWhenAnMappedExceptionOccurs(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .get("/test", (request, response) -> {
                            throw new IllegalArgumentException();
                        })
                        .configured(toMapExceptionsOfType(IllegalArgumentException.class, (exception, request, response) -> response.setStatus(501)))
                        .configured(toMapExceptionsByDefaultUsing((exception, request, response) -> response.setStatus(500)))
                        .configured(toActivateCORSWithoutValidatingTheOrigin()
                                .exposingTheResponseHeaders("Some-Header", "Another-Header", "Yet-Another-Header")
                                .withTimeOutAfter(10, TimeUnit.DAYS)
                                .allowingCredentials())
                        .build()
        )
                .when().aRequestToThePath("/test").viaTheGetMethod().withAnEmptyBody().withTheHeader("Origin", "localhost").isIssued()
                .theStatusCodeWas(501)
                .theReponseContainsTheHeader("Access-Control-Allow-Origin", "localhost")
                .theReponseContainsTheHeader("Access-Control-Allow-Credentials", "true")
                .theReponseContainsTheHeaderRawValue("Access-Control-Expose-Headers", "some-header, another-header, yet-another-header");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void corsPreflightRequestWorks(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .configured(toActivateCORSWithoutValidatingTheOrigin()
                                .withAllowedMethods(GET, POST, PUT, DELETE)
                                .withAllowedHeaders("X-Custom-Header", "Upgrade-Insecure-Requests"))
                        .build()
        )
                .when().aRequestToThePath("/the/path/does/not/matter/for/options/requests")
                .viaTheOptionsMethod().withAnEmptyBody().withTheHeader("Origin", "foo.bar")
                .withTheHeader("Access-Control-Request-Headers", "X-Custom-Header")
                .withTheHeader("Access-Control-Request-Method", "PUT").isIssued()
                .theStatusCodeWas(200)
                .theReponseContainsTheHeader("Access-Control-Allow-Headers", "x-custom-header")
                .theReponseContainsTheHeader("Access-Control-Allow-Methods", "PUT")
                .theResponseBodyWas("");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void corsCanAllowAllHeaders(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .configured(toActivateCORSWithoutValidatingTheOrigin()
                                .withAllowedMethods(GET, POST, PUT, DELETE)
                                .allowingAllHeaders()
                        )
                        .build()
        )
                .when().aRequestToThePath("/the/path/does/not/matter/for/options/requests")
                .viaTheOptionsMethod().withAnEmptyBody().withTheHeader("Origin", "foo.bar")
                .withTheHeader("Access-Control-Request-Headers", "X-Custom-Header")
                .withTheHeader("Access-Control-Request-Method", "PUT").isIssued()
                .theStatusCodeWas(200)
                .theReponseContainsTheHeader("Access-Control-Allow-Headers", "x-custom-header")
                .theReponseContainsTheHeader("Access-Control-Allow-Methods", "PUT")
                .theResponseBodyWas("");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void corsCanExposeAllHeaders(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .get("/test", (request, response) -> response.setBody("foo"))
                        .configured(toActivateCORSWithoutValidatingTheOrigin()
                                .exposingAllResponseHeaders()
                        )
                        .build()
        )
                .when().aRequestToThePath("/test").viaTheGetMethod().withAnEmptyBody().withTheHeader("Origin", "localhost").isIssued()
                .theStatusCodeWas(200)
                .theReponseContainsTheHeader("Access-Control-Allow-Origin", "localhost")
                .theReponseContainsTheHeaderRawValue("Access-Control-Expose-Headers", "*");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void corsCanProhibitCredentials(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .get("/test", (request, response) -> response.setBody("foo"))
                        .configured(toActivateCORSWithoutValidatingTheOrigin()
                                .notAllowingCredentials()
                        )
                        .build()
        )
                .when().aRequestToThePath("/test").viaTheGetMethod().withAnEmptyBody().withTheHeader("Origin", "localhost").isIssued()
                .theStatusCodeWas(200)
                .theReponseContainsTheHeader("Access-Control-Allow-Origin", "localhost")
                .theResponseDoesNotContainTheHeader("Access-Control-Allow-Credentials");
    }
}
