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

import de.quantummaid.httpmaid.handler.PageNotFoundException;
import de.quantummaid.httpmaid.tests.givenwhenthen.TestEnvironment;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static de.quantummaid.httpmaid.Configurators.toHandlePageNotFoundUsing;
import static de.quantummaid.httpmaid.HttpMaid.anHttpMaid;
import static de.quantummaid.httpmaid.exceptions.ExceptionConfigurators.toMapExceptionsOfType;
import static de.quantummaid.httpmaid.http.Http.StatusCodes.FORBIDDEN;
import static de.quantummaid.httpmaid.tests.givenwhenthen.TestEnvironments.ALL_ENVIRONMENTS;

public final class PageNotFoundSpecs {

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void pageNotFoundExceptionLeadsTo404ByDefault(final TestEnvironment testEnvironment) {
        testEnvironment.given(() ->
                anHttpMaid().build()
        )
                .when().aRequestToThePath("/foo").viaThePostMethod().withAnEmptyBody().isIssued()
                .theStatusCodeWas(404)
                .theResponseBodyContains("");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void pageNotFoundExceptionCanBeConfigured(final TestEnvironment testEnvironment) {
        testEnvironment.given(() ->
                anHttpMaid()
                        .configured(toHandlePageNotFoundUsing((request, response) -> {
                            response.setStatus(FORBIDDEN);
                            response.setBody("this does not exist");
                        }))
                        .build()
        )
                .when().aRequestToThePath("/foo").viaThePostMethod().withAnEmptyBody().isIssued()
                .theStatusCodeWas(403)
                .theResponseBodyContains("this does not exist");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void pageNotFoundExceptionContainsContext(final TestEnvironment testEnvironment) {
        testEnvironment.given(() ->
                anHttpMaid()
                        .configured(toMapExceptionsOfType(PageNotFoundException.class, (exception, response) -> response.setBody(exception.getMessage())))
                        .build()
        )
                .when().aRequestToThePath("/foo").viaThePostMethod().withAnEmptyBody().isIssued()
                .theResponseBodyContains("No handler found for path '/foo' and method 'POST'");
    }
}
