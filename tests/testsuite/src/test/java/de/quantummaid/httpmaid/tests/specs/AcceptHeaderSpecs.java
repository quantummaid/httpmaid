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

import java.util.Map;

import static de.quantummaid.httpmaid.HttpMaid.anHttpMaid;
import static de.quantummaid.httpmaid.tests.givenwhenthen.TestEnvironments.ALL_ENVIRONMENTS;

public final class AcceptHeaderSpecs {

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void acceptHeaderWithMultipleSupportedContentTypesInSingleHeader(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .get("/", (request, response) -> response.setBody(Map.of("key", "value")))
                        .build()
        )
                .when().aRequestToThePath("/").viaTheGetMethod().withAnEmptyBody().withContentType("application/json")
                .withHeaderOccuringMultipleTimesHavingDistinctValue("Accept", "text/html, application/xhtml+xml, application/xml;q=0.9, image/webp, */*;q=0.8").isIssued()
                .theStatusCodeWas(200)
                .theJsonResponseStrictlyEquals(Map.of("key", "value"));
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void acceptHeaderWithMultipleSupportedContentTypesInSeparateHeaders(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .get("/", (request, response) -> response.setBody(Map.of("key", "value")))
                        .build()
        )
                .when().aRequestToThePath("/").viaTheGetMethod().withAnEmptyBody().withContentType("application/json")
                .withHeaderOccuringMultipleTimesHavingDistinctValue("Accept",
                        "text/html",
                        "application/xhtml+xml",
                        "application/xml;q=0.9",
                        "image/webp",
                        "*/*;q=0.8").isIssued()
                .theStatusCodeWas(200)
                .theJsonResponseStrictlyEquals(Map.of("key", "value"));
    }
}
