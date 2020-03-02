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

package de.quantummaid.httpmaid.tests.lowlevel;

import de.quantummaid.httpmaid.tests.givenwhenthen.TestEnvironment;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static de.quantummaid.httpmaid.Configurators.toCustomizeResponsesUsing;
import static de.quantummaid.httpmaid.HttpMaid.anHttpMaid;
import static de.quantummaid.httpmaid.tests.lowlevel.LowLevelHttpMaidConfiguration.theLowLevelHttpMaidInstanceUsedForTesting;

public final class LowLevelSpecs {

    @ParameterizedTest
    @MethodSource(TestEnvironment.ALL_ENVIRONMENTS)
    public void testBodyOfAPostRequest(final TestEnvironment testEnvironment) {
        testEnvironment.given(theLowLevelHttpMaidInstanceUsedForTesting())
                .when().aRequestToThePath("/echo").viaThePostMethod().withTheBody("This is a post request.").isIssued()
                .theStatusCodeWas(200)
                .theResponseBodyWas("This is a post request.");
    }

    @ParameterizedTest
    @MethodSource(TestEnvironment.ALL_ENVIRONMENTS)
    public void testBodyOfAPutRequest(final TestEnvironment testEnvironment) {
        testEnvironment.given(theLowLevelHttpMaidInstanceUsedForTesting())
                .when().aRequestToThePath("/echo").viaThePutMethod().withTheBody("This is a put request.").isIssued()
                .theStatusCodeWas(200)
                .theResponseBodyWas("This is a put request.");
    }

    @ParameterizedTest
    @MethodSource(TestEnvironment.ALL_ENVIRONMENTS)
    public void testBodyOfADeleteRequest(final TestEnvironment testEnvironment) {
        testEnvironment.given(theLowLevelHttpMaidInstanceUsedForTesting())
                .when().aRequestToThePath("/echo").viaTheDeleteMethod().withTheBody("This is a delete request.").isIssued()
                .theStatusCodeWas(200)
                .theResponseBodyWas("This is a delete request.");
    }

    @ParameterizedTest
    @MethodSource(TestEnvironment.ALL_ENVIRONMENTS)
    public void testContentTypeInRequest(final TestEnvironment testEnvironment) {
        testEnvironment.given(theLowLevelHttpMaidInstanceUsedForTesting())
                .when().aRequestToThePath("/echo_contenttype").viaTheGetMethod().withAnEmptyBody().withContentType("foobar").isIssued()
                .theStatusCodeWas(200)
                .theResponseBodyWas("foobar");
    }

    @ParameterizedTest
    @MethodSource(TestEnvironment.ALL_ENVIRONMENTS)
    public void testRequestContentTypeIsCaseInsensitive(final TestEnvironment testEnvironment) {
        testEnvironment.given(theLowLevelHttpMaidInstanceUsedForTesting())
                .when().aRequestToThePath("/echo_contenttype").viaTheGetMethod().withAnEmptyBody()
                .withTheHeader("CONTENT-TYPE", "foo").isIssued()
                .theStatusCodeWas(200)
                .theResponseBodyWas("foo");
    }

    @ParameterizedTest
    @MethodSource(TestEnvironment.ALL_ENVIRONMENTS)
    public void testContentTypeInResponse(final TestEnvironment testEnvironment) {
        testEnvironment.given(theLowLevelHttpMaidInstanceUsedForTesting())
                .when().aRequestToThePath("/set_contenttype_in_response").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theStatusCodeWas(200)
                .theResponseContentTypeWas("foobar");
    }

    @ParameterizedTest
    @MethodSource(TestEnvironment.ALL_ENVIRONMENTS)
    public void testHeadersInResponse(final TestEnvironment testEnvironment) {
        testEnvironment.given(theLowLevelHttpMaidInstanceUsedForTesting())
                .when().aRequestToThePath("/headers_response").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theStatusCodeWas(200)
                .theReponseContainsTheHeader("foo", "bar");
    }

    @ParameterizedTest
    @MethodSource(TestEnvironment.ALL_ENVIRONMENTS)
    public void testLoggerCanBeSet(final TestEnvironment testEnvironment) {
        testEnvironment.given(theLowLevelHttpMaidInstanceUsedForTesting())
                .when().aRequestToThePath("/log").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theStatusCodeWas(200)
                .theLogOutputStartedWith("INFO: foobar");
    }

    @ParameterizedTest
    @MethodSource(TestEnvironment.ALL_ENVIRONMENTS)
    public void testFileDownload(final TestEnvironment testEnvironment) {
        testEnvironment.given(theLowLevelHttpMaidInstanceUsedForTesting())
                .when().aRequestToThePath("/download").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theStatusCodeWas(200)
                .theResponseContentTypeWas("application/x-msdownload")
                .theResponseBodyWas("download-content")
                .theReponseContainsTheHeader("Content-Disposition", "attachment; filename=\"foo.txt\"");
    }

    @ParameterizedTest
    @MethodSource(TestEnvironment.ALL_ENVIRONMENTS)
    public void testDebugModule(final TestEnvironment testEnvironment) {
        testEnvironment.given(theLowLevelHttpMaidInstanceUsedForTesting())
                .when().aRequestToThePath("/internals").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theStatusCodeWas(200)
                .theResponseBodyContains("digraph");
    }

    @ParameterizedTest
    @MethodSource(TestEnvironment.ALL_ENVIRONMENTS)
    public void testEmptyTemplate(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .get("/test", (request, response) -> response.setBody("OK"))
                        .configured(toCustomizeResponsesUsing(metaData -> {
                        }))
                        .build()
        )
                .when().aRequestToThePath("/test").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theStatusCodeWas(200)
                .theResponseBodyWas("OK");
    }
}
