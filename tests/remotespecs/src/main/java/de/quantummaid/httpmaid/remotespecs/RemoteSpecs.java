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

package de.quantummaid.httpmaid.remotespecs;

import de.quantummaid.httpmaid.tests.givenwhenthen.TestEnvironment;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public interface RemoteSpecs {

    RemoteSpecsDeployer provideDeployer();

    @Test
    default void httpTest(final TestEnvironment testEnvironment) {
        testEnvironment.givenTheStaticallyDeployedTestInstance()
                .when().aRequestToThePath("/").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theStatusCodeWas(200)
                .theResponseBodyWas("fooooo");
    }

    @Test
    default void bodyCanContainJson(final TestEnvironment testEnvironment) {
        testEnvironment.givenTheStaticallyDeployedTestInstance()
                .when().aRequestToThePath("/jsonResponse").viaTheGetMethod().withAnEmptyBody()
                .withContentType("application/json").isIssued()
                .theStatusCodeWas(201)
                .theResponseBodyWas("{\"foo\":\"bar\"}");
    }

    @Test
    default void canReceiveSingleHeader(final TestEnvironment testEnvironment) {
        testEnvironment.givenTheStaticallyDeployedTestInstance()
                .when().aRequestToThePath("/returnHeader/X-My-Header").viaTheGetMethod().withAnEmptyBody()
                .withTheHeader("X-My-Header", "foo").isIssued()
                .theStatusCodeWas(200)
                .theResponseBodyWas("foo");
    }

    @Test
    default void canReceiveDuplicatedHeaderWithDistinctValues(final TestEnvironment testEnvironment) {
        testEnvironment.givenTheStaticallyDeployedTestInstance()
                .when().aRequestToThePath("/").viaTheGetMethod().withAnEmptyBody()
                .withHeaderOccuringMultipleTimesHavingDistinctValue("X-My-Header", "foo", "bar")
                .isIssued()
                .theStatusCodeWas(200)
                .theResponseBodyWas("foo");
    }

    @Test
    default void canReceiveQueryParameter() {

    }

    @Test
    default void canReceiveQueryParameterWithMultipleValues() {

    }

    // TODO cookies

    @Test
    default void handlersCanSetStatusCode(final TestEnvironment testEnvironment) {
        testEnvironment.givenTheStaticallyDeployedTestInstance()
                .when().aRequestToThePath("/statusCode/201").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theStatusCodeWas(201)
                .theResponseBodyWas("");
    }

    @Test
    default void handlersCanSetSingleValueHeader(final TestEnvironment testEnvironment) {
        testEnvironment.givenTheStaticallyDeployedTestInstance()
                .when().aRequestToThePath("/headers/HeaderName/HeaderValue").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theReponseContainsTheHeader("HeaderName", "HeaderValue")
                .theResponseBodyWas("");
    }

    @Test
    default void handlersCanSetMultiValueHeader(final TestEnvironment testEnvironment) {
        testEnvironment.givenTheStaticallyDeployedTestInstance()
                .when().aRequestToThePath("/multiValueHeaders/HeaderName/HeaderValue1,HeaderValue2").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theStatusCodeWas(200)
                .theReponseContainsTheHeader("HeaderName", "HeaderValue1", "HeaderValue2")
                .theResponseBodyWas("");
    }

    @Disabled
    @Test
    default void websocketTest(final TestEnvironment testEnvironment) {
        testEnvironment.givenTheStaticallyDeployedTestInstance()
                .when().aWebsocketIsConnected()
                .andWhen().aWebsocketMessageIsSent("{ \"message\": \"handler2\" }")
                .aWebsocketMessageHasBeenReceivedWithContent("handler 2");
    }

    @Test
    @Disabled
    default void handlersCanBroadcast(final TestEnvironment testEnvironment) {
        testEnvironment.givenTheStaticallyDeployedTestInstance()
                .when().aWebsocketIsConnected()
                .andWhen().aWebsocketMessageIsSent("{ \"message\": \"check\" }")
                .aWebsocketMessageHasBeenReceivedWithContent("websocket has been registered")
                .andWhen().aRequestToThePath("/broadcast").viaThePostMethod().withTheBody("{ \"message\": \"foo\" }").isIssued()
                .theStatusCodeWas(200)
                .aWebsocketMessageHasBeenReceivedWithContent("foo");
    }

    /**
     * https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Cookie
     * Cookie: <cookie-list>
     * Cookie: name=value; name2=value2; name3=value3
     * <cookie-list> A list of name-value pairs in the form of <cookie-name>=<cookie-value>.
     *  Pairs in the list are separated by a semicolon and a space ('; ').
     */
    @Test
    default void requestCanContainMultipleCookiesInOneCookieHeader(final TestEnvironment testEnvironment) {
        testEnvironment.givenTheStaticallyDeployedTestInstance()
                .when().aRequestToThePath("/cookie").viaTheGetMethod().withAnEmptyBody()
                .withTheHeader("Cookie", "cookie1=qwer; cookie2=asdf").isIssued()
                .theResponseBodyWas("qwer and asdf");
    }

    /**
     * https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Set-Cookie
     * A <cookie-value> can optionally be wrapped in double quotes and include any US-ASCII characters
     * excluding control characters, Whitespace, double quotes, comma, semicolon, and backslash.
     * Encoding: Many implementations perform URL encoding on cookie values, however it is not
     * required per the RFC specification. It does help satisfying the requirements about which
     * characters are allowed for <cookie-value> though.
     */
    @Test
    default void responseCanContainMultipleCookies(final TestEnvironment testEnvironment) {
        testEnvironment.givenTheStaticallyDeployedTestInstance()
                .when().aRequestToThePath("/setcookies").viaTheGetMethod().withAnEmptyBody()
                .isIssued()
                .theReponseContainsTheHeader("Set-Cookie", "name=\"value\"", "name2=\"value2\"")
                .theResponseBodyWas("");
    }
}
