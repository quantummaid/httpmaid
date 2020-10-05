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

import de.quantummaid.httpmaid.http.HttpRequestException;
import de.quantummaid.httpmaid.tests.givenwhenthen.TestEnvironment;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;

import static de.quantummaid.httpmaid.HttpMaid.anHttpMaid;
import static de.quantummaid.httpmaid.exceptions.ExceptionConfigurators.toMapExceptionsOfType;
import static de.quantummaid.httpmaid.tests.givenwhenthen.TestEnvironments.ALL_ENVIRONMENTS;

public final class HeaderSpecs {

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void testHeadersInResponse(final TestEnvironment testEnvironment) {
        testEnvironment.given(() ->
                anHttpMaid()
                        .get("/headers_response", (request, response) -> response.addHeader("name+1 %ü", "value+1 %ü"))
                        .build()
        )
                .when().aRequestToThePath("/headers_response").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theStatusCodeWas(200)
                .theReponseContainsTheHeader("name+1 %ü", "value+1 %ü");
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

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void aRequestHeaderThatOccursMultipleTimesWithDifferentValues(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .get("/<name>", (request, response) -> {
                            final String name = request.pathParameters().getPathParameter("name");
                            final List<String> values = request.headers().allValuesFor(name);
                            final String joined = String.join("+", values);
                            response.setBody(joined);
                        })
                        .build()
        )
                .when().aRequestToThePath("/X-HeaderName").viaTheGetMethod().withAnEmptyBody()
                .withHeaderOccuringMultipleTimesHavingDistinctValue("X-Headername", "value1", "value2").isIssued()
                .theStatusCodeWas(200)
                .theResponseBodyWas("value1+value2");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void exceptionWhenAccessingNotExistingHeader(final TestEnvironment testEnvironment) {
        testEnvironment.given(() ->
                anHttpMaid()
                        .get("/", (request, response) -> request.headers().header("not_existing"))
                        .configured(toMapExceptionsOfType(HttpRequestException.class, (exception, response) -> {
                            response.setBody(exception.getMessage());
                            response.setStatus(501);
                        }))
                        .build()
        )
                .when().aRequestToThePath("/").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theStatusCodeWas(501)
                .theResponseBodyWas("No header with name 'not_existing'");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void exceptionWhenHandlerAccessesMultiValueQueryParameterButExpectsSingleValue(final TestEnvironment testEnvironment) {
        testEnvironment.given(() ->
                anHttpMaid()
                        .get("/", (request, response) -> request.headers().optionalHeader("multiple_values"))
                        .configured(toMapExceptionsOfType(HttpRequestException.class, (exception, response) -> {
                            response.setBody(exception.getMessage());
                            response.setStatus(501);
                        }))
                        .build()
        )
                .when().aRequestToThePath("/").viaTheGetMethod().withAnEmptyBody()
                .withHeaderOccuringMultipleTimesHavingDistinctValue("multiple_values", "value1", "value2", "value3").isIssued()
                .theStatusCodeWas(501)
                .theResponseBodyWas("Expecting header 'multiple_values' to only have one value but got [value1, value2, value3]");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void headersCanBeDumpedAsMap(final TestEnvironment testEnvironment) {
        testEnvironment.given(() ->
                anHttpMaid()
                        .get("/", (request, response) -> {
                            final String asMap = request.headers().asMap().toString();
                            response.setBody(asMap);
                        })
                        .build()
        )
                .when().aRequestToThePath("/").viaTheGetMethod().withAnEmptyBody()
                .withTheHeader("key", "value").isIssued()
                .theStatusCodeWas(200)
                .theResponseBodyContains("key=[value]");
    }
}
