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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static de.quantummaid.httpmaid.HttpMaid.anHttpMaid;
import static de.quantummaid.httpmaid.http.Http.StatusCodes.UNAUTHORIZED;
import static de.quantummaid.httpmaid.security.SecurityConfigurators.toAuthorizeRequestsUsing;
import static de.quantummaid.httpmaid.tests.givenwhenthen.TestEnvironments.ALL_ENVIRONMENTS;

public final class AuthorizationSpecs {

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void unauthorizedRequestsCanBeRejected(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .get("/secret", (request, response) -> response.setBody("the secret"))
                        .configured(toAuthorizeRequestsUsing((authenticationInformation, request) -> false))
                        .build()
        )
                .when().aRequestToThePath("/secret").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theStatusCodeWas(500)
                .theResponseBodyWas("");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void rejectionCanBeConfigured(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .get("/secret", (request, response) -> response.setBody("the secret"))
                        .configured(toAuthorizeRequestsUsing((authenticationInformation, request) -> false)
                                .rejectingUnauthorizedRequestsUsing((request, response) -> {
                                    response.setStatus(UNAUTHORIZED);
                                    response.setBody("You have been rejected");
                                }))
                        .build()
        )
                .when().aRequestToThePath("/secret").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theStatusCodeWas(401)
                .theResponseBodyWas("You have been rejected");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void routesCanBeExcludedFromAuthorization(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .get("/secret", (request, response) -> response.setBody("the secret"))
                        .configured(toAuthorizeRequestsUsing((authenticationInformation, request) -> false).exceptRequestsTo("/secret"))
                        .build()
        )
                .when().aRequestToThePath("/secret").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theStatusCodeWas(200)
                .theResponseBodyWas("the secret");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void authorizationCanBeLimitedToCertainRoutes(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .get("/secret", (request, response) -> response.setBody("the secret"))
                        .configured(toAuthorizeRequestsUsing((authenticationInformation, request) -> false).onlyRequestsTo("/somethingElse"))
                        .build()
        )
                .when().aRequestToThePath("/secret").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theStatusCodeWas(200)
                .theResponseBodyWas("the secret");
    }
}
