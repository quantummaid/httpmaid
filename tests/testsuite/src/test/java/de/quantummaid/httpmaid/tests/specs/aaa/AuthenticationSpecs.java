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

package de.quantummaid.httpmaid.tests.specs.aaa;

import de.quantummaid.httpmaid.tests.givenwhenthen.TestEnvironment;
import de.quantummaid.httpmaid.tests.specs.aaa.domain.AuthenticatedUseCase;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Optional;

import static de.quantummaid.httpmaid.HttpMaid.anHttpMaid;
import static de.quantummaid.httpmaid.events.EventConfigurators.mappingAuthenticationInformation;
import static de.quantummaid.httpmaid.security.SecurityConfigurators.*;
import static de.quantummaid.httpmaid.tests.specs.aaa.domain.User.user;
import static java.util.Optional.of;

public final class AuthenticationSpecs {

    @ParameterizedTest
    @MethodSource(TestEnvironment.ALL_ENVIRONMENTS)
    public void requestsCanBeAuthenticatedWithHeader(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .get("/username", (request, response) -> {
                            final String username = request.optionalAuthenticationInformationAs(String.class).orElseThrow();
                            response.setBody(username);
                        })
                        .configured(toAuthenticateUsingHeader("username", Optional::of))
                        .build()
        )
                .when().aRequestToThePath("/username").viaTheGetMethod().withAnEmptyBody().withTheHeader("username", "asdf").isIssued()
                .theResponseBodyWas("asdf");
    }

    @ParameterizedTest
    @MethodSource(TestEnvironment.ALL_ENVIRONMENTS)
    public void requestsCanBeAuthenticatedWithCookie(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .get("/username", (request, response) -> {
                            final String username = request.optionalAuthenticationInformationAs(String.class).orElseThrow();
                            response.setBody(username);
                        })
                        .configured(toAuthenticateUsingCookie("username", Optional::of))
                        .build()
        )
                .when().aRequestToThePath("/username").viaTheGetMethod().withAnEmptyBody().withTheHeader("Cookie", "username=asdf").isIssued()
                .theResponseBodyWas("asdf");
    }

    @ParameterizedTest
    @MethodSource(TestEnvironment.ALL_ENVIRONMENTS)
    public void requestsCanBeAuthenticatedWithQueryParameter(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .get("/username", (request, response) -> {
                            final String username = request.optionalAuthenticationInformationAs(String.class).orElseThrow();
                            response.setBody(username);
                        })
                        .configured(toAuthenticateUsingQueryParameter("username", Optional::of))
                        .build()
        )
                .when().aRequestToThePath("/username?username=asdf").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theResponseBodyWas("asdf");
    }

    @ParameterizedTest
    @MethodSource(TestEnvironment.ALL_ENVIRONMENTS)
    public void requestsCanBeAuthenticatedWithPathParameter(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .get("/username/<username>", (request, response) -> {
                            final String username = request.optionalAuthenticationInformationAs(String.class).orElseThrow();
                            response.setBody(username);
                        })
                        .configured(toAuthenticateUsingPathParameter("username", Optional::of))
                        .build()
        )
                .when().aRequestToThePath("/username/asdf").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theResponseBodyWas("asdf");
    }

    @ParameterizedTest
    @MethodSource(TestEnvironment.ALL_ENVIRONMENTS)
    public void requestsCanBeAuthenticatedWithBody(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .post("/username", (request, response) -> {
                            final String username = request.optionalAuthenticationInformationAs(String.class).orElseThrow();
                            response.setBody(username);
                        })
                        .configured(toAuthenticateRequestsUsing(request -> of(request.bodyString())).afterBodyProcessing())
                        .build()
        )
                .when().aRequestToThePath("/username").viaThePostMethod().withTheBody("\"asdf\"").isIssued()
                .theResponseBodyWas("\"asdf\"");
    }

    @ParameterizedTest
    @MethodSource(TestEnvironment.ALL_ENVIRONMENTS)
    public void requestsCanBeAuthenticatedWithOAuth2BearerToken(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .get("/username", (request, response) -> {
                            final String username = request.optionalAuthenticationInformationAs(String.class).orElseThrow();
                            response.setBody(username);
                        })
                        .configured(toAuthenticateUsingOAuth2BearerToken(Optional::of))
                        .build()
        )
                .when().aRequestToThePath("/username").viaTheGetMethod().withAnEmptyBody().withTheHeader("Authorization", "Bearer asdf").isIssued()
                .theResponseBodyWas("asdf");
    }

    @ParameterizedTest
    @MethodSource(TestEnvironment.ALL_ENVIRONMENTS)
    public void routesCanBeExcludedFromAuthentication(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .get("/username", (request, response) -> {
                            final String username = request.optionalAuthenticationInformationAs(String.class).orElse("guest");
                            response.setBody(username);
                        })
                        .configured(toAuthenticateUsingHeader("username", Optional::of).exceptRequestsTo("/username", "/somethingElse"))
                        .build()
        )
                .when().aRequestToThePath("/username").viaTheGetMethod().withAnEmptyBody().withTheHeader("username", "asdf").isIssued()
                .theResponseBodyWas("guest");
    }

    @ParameterizedTest
    @MethodSource(TestEnvironment.ALL_ENVIRONMENTS)
    public void authenticationCanBeLimitedToCertainRoutes(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .get("/username", (request, response) -> {
                            final String username = request.optionalAuthenticationInformationAs(String.class).orElse("guest");
                            response.setBody(username);
                        })
                        .configured(toAuthenticateUsingHeader("username", Optional::of).onlyRequestsTo("/somethingElse"))
                        .build()
        )
                .when().aRequestToThePath("/username").viaTheGetMethod().withAnEmptyBody().withTheHeader("username", "asdf").isIssued()
                .theResponseBodyWas("guest");
    }

    @ParameterizedTest
    @MethodSource(TestEnvironment.ALL_ENVIRONMENTS)
    public void authenticationCanBeADomainObject(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .get("/", AuthenticatedUseCase.class, mappingAuthenticationInformation())
                        .configured(toAuthenticateUsingHeader("user", challenge -> Optional.of(user(challenge))))
                        .build()
        )
                .when().aRequestToThePath("/").viaTheGetMethod().withAnEmptyBody().withTheHeader("user", "foo").isIssued()
                .theResponseBodyWas("\"authenticated as foo\"");
    }
}
