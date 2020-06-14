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

package de.quantummaid.httpmaid.tests.specs.headers;

import de.quantummaid.httpmaid.tests.givenwhenthen.TestEnvironment;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Map;

import static de.quantummaid.httpmaid.HttpMaid.anHttpMaid;
import static de.quantummaid.httpmaid.tests.givenwhenthen.TestEnvironments.ALL_ENVIRONMENTS;

public final class HeaderSpecs {

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void testHeadersInResponse(final TestEnvironment testEnvironment) {
        testEnvironment.given(() ->
                anHttpMaid()
                        .get("/headers_response", (request, response) -> response.addHeader("foo", "bar"))
                        .build()
        )
                .when().aRequestToThePath("/headers_response").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theStatusCodeWas(200)
                .theReponseContainsTheHeader("foo", "bar");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void requestHeadersCanBeAMap(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .get("/test", (request, response) -> {
                            final Map<String, String> headersMap = request.headers().asStringMap();
                            response.setBody(headersMap.toString());
                        })
                        .build()
        )
                .when().aRequestToThePath("/test").viaTheGetMethod().withAnEmptyBody()
                .withTheHeader("a", "1").withTheHeader("b", "2").withTheHeader("c", "3")
                .isIssued()
                .theStatusCodeWas(200)
                .theResponseBodyContains("a=1")
                .theResponseBodyContains("b=2")
                .theResponseBodyContains("c=3");
    }


    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void handlersCanSetMultiValueHeader(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .get("/multiValueHeaders/HeaderName/HeaderValue1,HeaderValue2", (request, response) -> {
                            response.addHeader("HeaderName", "HeaderValue1");
                            response.addHeader("HeaderName", "HeaderValue2");
                        })
                        .build()
        )
                .when().aRequestToThePath("/multiValueHeaders/HeaderName/HeaderValue1,HeaderValue2").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theStatusCodeWas(200)
                .theReponseContainsTheHeader("HeaderName", "HeaderValue1", "HeaderValue2")
                .theResponseBodyWas("");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void handlersCanSetMultiValueHeaderBySeparatingValuesWithAComma(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .get("/", (request, response) ->
                                response.addHeader("X-HeaderName", "HeaderValue1,HeaderValue2"))
                        .build()
        )
                .when().aRequestToThePath("/").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theStatusCodeWas(200)
                .theReponseContainsTheHeader("X-HeaderName", "HeaderValue1", "HeaderValue2")
                .theResponseBodyWas("");
    }
}
