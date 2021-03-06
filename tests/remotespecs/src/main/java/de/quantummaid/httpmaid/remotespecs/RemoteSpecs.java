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
import de.quantummaid.httpmaid.tests.givenwhenthen.builders.HeaderBuilder;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public interface RemoteSpecs {
    RemoteSpecsDeployer provideDeployer();

    default Optional<String> accessToken() {
        return Optional.empty();
    }

    default Optional<String> additionInformationOnError() {
        return Optional.empty();
    }

    default Map<String, List<String>> mapWithAccessToken() {
        return accessToken()
                .map(accessToken -> Map.of("access_token", List.of(accessToken)))
                .orElseGet(Map::of);
    }

    default Consumer<HeaderBuilder> authorizationHeader() {
        return accessToken()
                .map(accessToken -> String.format("Bearer %s", accessToken))
                .map(header -> (Consumer<HeaderBuilder>) headerBuilder ->
                        headerBuilder.withTheHeader("Authorization", header))
                .orElse(null);
    }

    @Test
    default void httpTest(final TestEnvironment testEnvironment) {
        testEnvironment.givenTheStaticallyDeployedTestInstance()
                .when().aRequestToThePath("/").viaTheGetMethod().withAnEmptyBody().withTheOptionalHeader(authorizationHeader()).isIssued()
                .theStatusCodeWas(200)
                .theResponseBodyWas("fooooo");
    }

    @Test
    default void useCaseCanBeHandled(final TestEnvironment testEnvironment) {
        testEnvironment.givenTheStaticallyDeployedTestInstance()
                .when().aRequestToThePath("/usecase").viaThePostMethod()
                .withTheBody("{ \"id\": \"abc\", \"dto\": { \"fieldA\": \"foo\", \"fieldB\": \"bar\" } }")
                .withTheOptionalHeader(authorizationHeader())
                .withContentType("application/json").isIssued()
                .theStatusCodeWas(200)
                .theJsonResponseEquals("{\"value\":{\"fieldB\":\"bar\",\"fieldA\":\"foo\"},\"id\":\"abc\"}");
    }

    @Test
    default void handlerCanReceiveBodyOfPostRequest(final TestEnvironment testEnvironment) {
        testEnvironment.givenTheStaticallyDeployedTestInstance()
                .when().aRequestToThePath("/echo").viaThePostMethod().withTheBody("This is a post request.")
                .withTheOptionalHeader(authorizationHeader()).isIssued()
                .theStatusCodeWas(200)
                .theResponseBodyWas("This is a post request.");
    }

    @Test
    default void handlerCanReceiveBodyOfPutRequest(final TestEnvironment testEnvironment) {
        testEnvironment.givenTheStaticallyDeployedTestInstance()
                .when().aRequestToThePath("/echo").viaThePutMethod().withTheBody("This is a post request.")
                .withTheOptionalHeader(authorizationHeader()).isIssued()
                .theStatusCodeWas(200)
                .theResponseBodyWas("This is a post request.");
    }

    @Test
    default void handlerCanReceiveBodyOfDeleteRequest(final TestEnvironment testEnvironment) {
        testEnvironment.givenTheStaticallyDeployedTestInstance()
                .when().aRequestToThePath("/echo").viaTheDeleteMethod().withTheBody("This is a post request.")
                .withTheOptionalHeader(authorizationHeader()).isIssued()
                .theStatusCodeWas(200)
                .theResponseBodyWas("This is a post request.");
    }

    @Test
    default void bodyCanContainJson(final TestEnvironment testEnvironment) {
        testEnvironment.givenTheStaticallyDeployedTestInstance()
                .when().aRequestToThePath("/jsonResponse").viaTheGetMethod().withAnEmptyBody()
                .withContentType("application/json").withTheOptionalHeader(authorizationHeader()).isIssued()
                .theStatusCodeWas(201)
                .theResponseBodyWas("{\"foo\":\"bar\"}");
    }

    @Test
    default void canReceiveSingleHeader(final TestEnvironment testEnvironment) {
        testEnvironment.givenTheStaticallyDeployedTestInstance()
                .when().aRequestToThePath("/returnHeader/X-My-Header").viaTheGetMethod().withAnEmptyBody().withContentType("application/json")
                .withTheHeader("X-My-Header", "foo").withTheOptionalHeader(authorizationHeader()).isIssued()
                .theStatusCodeWas(200)
                .theJsonResponseStrictlyEquals(Map.of("headers", List.of("foo")));
    }

    @Test
    default void canReceiveDuplicatedHeaderWithDistinctValues(final TestEnvironment testEnvironment) {
        testEnvironment.givenTheStaticallyDeployedTestInstance()
                .when().aRequestToThePath("/returnHeader/X-My-Header").viaTheGetMethod().withAnEmptyBody()
                .withHeaderOccuringMultipleTimesHavingDistinctValue("X-My-Header", "foo", "bar")
                .withTheOptionalHeader(authorizationHeader()).isIssued()
                .theStatusCodeWas(200)
                .theJsonResponseStrictlyEquals(Map.of("headers", List.of("foo", "bar")));
    }

    @Test
    default void doesNotDieWhenSendingAnAcceptHeaderWithMultipleSupportedContentTypes(final TestEnvironment testEnvironment) {
        testEnvironment.givenTheStaticallyDeployedTestInstance()
                .when().aRequestToThePath("/jsonResponse").viaTheGetMethod().withAnEmptyBody().withContentType("application/json")
                .withHeaderOccuringMultipleTimesHavingDistinctValue("Accept", "text/html, application/xhtml+xml, application/xml;q=0.9, image/webp, */*;q=0.8")
                .withTheOptionalHeader(authorizationHeader()).isIssued()
                .theStatusCodeWas(201);
    }

    @Test
    default void canReceiveQueryParameter(final TestEnvironment testEnvironment) {
        testEnvironment.givenTheStaticallyDeployedTestInstance()
                .when().aRequestToThePath("/dumpQueryParameters").viaTheGetMethod()
                .withAnEmptyBody().withQueryStringParameter("param+1 %ü", "value+1 %ü")
                .withTheOptionalHeader(authorizationHeader()).isIssued()
                .theJsonResponseStrictlyEquals(Map.of("param+1 %ü", List.of("value+1 %ü")));
    }

    @Test
    default void canReceiveQueryParameterWithMultipleValues(final TestEnvironment testEnvironment) {
        testEnvironment.givenTheStaticallyDeployedTestInstance()
                .when().aRequestToThePath("/dumpQueryParameters").viaTheGetMethod()
                .withAnEmptyBody()
                .withQueryStringParameter("param1", "value1")
                .withQueryStringParameter("otherparam", "othervalue")
                .withQueryStringParameter("param1", "value2")
                .withTheOptionalHeader(authorizationHeader()).isIssued()
                .theJsonResponseStrictlyEquals(
                        Map.of("param1", List.of("value1", "value2"),
                                "otherparam", List.of("othervalue")));
    }

    @Test
    default void handlersCanSetStatusCode(final TestEnvironment testEnvironment) {
        testEnvironment.givenTheStaticallyDeployedTestInstance()
                .when().aRequestToThePath("/statusCode/201").viaTheGetMethod().withAnEmptyBody()
                .withTheOptionalHeader(authorizationHeader()).isIssued()
                .theStatusCodeWas(201)
                .theResponseBodyWas("");
    }

    @Test
    default void handlersCanSetSingleValueHeader(final TestEnvironment testEnvironment) {
        testEnvironment.givenTheStaticallyDeployedTestInstance()
                .when().aRequestToThePath("/headers/HeaderName/HeaderValue").viaTheGetMethod().withAnEmptyBody()
                .withTheOptionalHeader(authorizationHeader()).isIssued()
                .theReponseContainsTheHeader("HeaderName", "HeaderValue")
                .theResponseBodyWas("");
    }

    @Test
    default void handlersCanSetMultiValueHeader(final TestEnvironment testEnvironment) {
        testEnvironment.givenTheStaticallyDeployedTestInstance()
                .when().aRequestToThePath("/multiValueHeaders/HeaderName/HeaderValue1,HeaderValue2").viaTheGetMethod().withAnEmptyBody()
                .withTheOptionalHeader(authorizationHeader()).isIssued()
                .theStatusCodeWas(200)
                .theReponseContainsTheHeader("HeaderName", "HeaderValue1", "HeaderValue2")
                .theResponseBodyWas("");
    }

    /**
     * Cookie: cookie-list
     * Cookie: name=value; name2=value2; name3=value3
     * cookie-list A list of name-value pairs in the form of cookie-name=cookie-value.
     * Pairs in the list are separated by a semicolon and a space ('; ').
     * https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Cookie
     */
    @Test
    default void requestCanContainMultipleCookiesInOneCookieHeader(final TestEnvironment testEnvironment) {
        testEnvironment.givenTheStaticallyDeployedTestInstance()
                .when().aRequestToThePath("/cookie").viaTheGetMethod().withAnEmptyBody()
                .withTheHeader("Cookie", "cookie1=qwer; cookie2=asdf")
                .withTheOptionalHeader(authorizationHeader()).isIssued()
                .theResponseBodyWas("qwer and asdf");
    }

    /**
     * A cookie-value can optionally be wrapped in double quotes and include any US-ASCII characters
     * excluding control characters, Whitespace, double quotes, comma, semicolon, and backslash.
     * Encoding: Many implementations perform URL encoding on cookie values, however it is not
     * required per the RFC specification. It does help satisfying the requirements about which
     * characters are allowed for cookie-value though.
     * https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Set-Cookie
     */
    @Test
    default void responseCanContainMultipleCookies(final TestEnvironment testEnvironment) {
        testEnvironment.givenTheStaticallyDeployedTestInstance()
                .when().aRequestToThePath("/setcookies").viaTheGetMethod().withAnEmptyBody()
                .withTheOptionalHeader(authorizationHeader()).isIssued()
                .theReponseContainsTheHeader("Set-Cookie",
                        "name=\"value\"",
                        "name2=\"value2\""
                )
                .theResponseBodyWas("");
    }

    @Test
    default void responseCanContainMultipleCookiesWithCommas(final TestEnvironment testEnvironment) {
        testEnvironment.givenTheStaticallyDeployedTestInstance()
                .when().aRequestToThePath("/setCookiesWithCommas").viaTheGetMethod().withAnEmptyBody()
                .withTheOptionalHeader(authorizationHeader()).isIssued()
                .theReponseContainsTheHeaderRawValues("Set-Cookie",
                        "cookie1=\"cookie,value,1\"",
                        "cookie2=\"cookie,value,2\"");
    }

    @Test
    default void websocketTest(final TestEnvironment testEnvironment) {
        testEnvironment.givenTheStaticallyDeployedTestInstance()
                .when().aWebsocketIsConnected(mapWithAccessToken(), Map.of())
                .andWhen().aWebsocketMessageIsSent("{ \"message\": \"handler2\" }")
                .allWebsocketsHaveReceivedTheMessage("handler 2");
    }

    @Test
    default void websocketsCanAccessHeaders(final TestEnvironment testEnvironment) {
        testEnvironment.givenTheStaticallyDeployedTestInstance()
                .when().aWebsocketIsConnected(
                mapWithAccessToken(),
                Map.of("X-My-Header", List.of("myvalue")))
                .andWhen().aWebsocketMessageIsSent("{ \"message\": \"headerhandler\" }")
                .allWebsocketsHaveReceivedTheMessage("myvalue");
    }

    @Test
    default void websocketsCanAccessQueryParameters(final TestEnvironment testEnvironment) {
        final Map<String, List<String>> queryParameters = new LinkedHashMap<>(mapWithAccessToken());
        queryParameters.put("myParameter", List.of("myValue"));
        testEnvironment.givenTheStaticallyDeployedTestInstance()
                .when().aWebsocketIsConnected(queryParameters, Map.of())
                .andWhen().aWebsocketMessageIsSent("{ \"message\": \"queryparameterhandler\" }")
                .allWebsocketsHaveReceivedTheMessage("myvalue");
    }

    @Test
    default void handlersCanBroadcast(final TestEnvironment testEnvironment) {
        testEnvironment.givenTheStaticallyDeployedTestInstance()
                .when().aWebsocketIsConnected(mapWithAccessToken(), Map.of())
                .andWhen().aWebsocketMessageIsSent("{ \"message\": \"check\" }")
                .allWebsocketsHaveReceivedTheMessage("websocket has been registered")
                .andWhen().aRequestToThePath("/broadcast").viaThePostMethod().withTheBody("{ \"message\": \"foo\" }")
                .withTheOptionalHeader(authorizationHeader()).isIssued()
                .theStatusCodeWas(200)
                .allWebsocketsHaveReceivedTheMessage("foo");
    }

    @Test
    default void handlersCanDisconnectWebsockets(final TestEnvironment testEnvironment) {
        testEnvironment.givenTheStaticallyDeployedTestInstance()
                .when().aWebsocketIsConnected(mapWithAccessToken(), Map.of())
                .andWhen().aWebsocketMessageIsSent("{ \"message\": \"check\" }")
                .allWebsocketsHaveReceivedTheMessage("websocket has been registered")
                .andWhen().aWebsocketMessageIsSent("{ \"message\": \"check\" }")
                .allWebsocketsHaveReceivedTheMessage("websocket has been registered")
                .andWhen().aWebsocketMessageIsSent("{ \"message\": \"check\" }")
                .allWebsocketsHaveReceivedTheMessage("websocket has been registered")
                .andWhen().aWebsocketMessageIsSent("{ \"message\": \"disconnect\" }")
                .allWebsocketsHaveBeenClosed();
    }

    @Test
    default void additionalDataCanBeStoredInWebsocketRegistry(final TestEnvironment testEnvironment) {
        final Map<String, List<String>> queryParameters = new LinkedHashMap<>(mapWithAccessToken());
        queryParameters.put("toBeStoredInContext", List.of("foobar"));
        testEnvironment.givenTheStaticallyDeployedTestInstance()
                .when().aWebsocketIsConnected(queryParameters, Map.of())
                .andWhen().aWebsocketMessageIsSent("{ \"message\": \"echofromcontext\" }")
                .allWebsocketsHaveReceivedTheMessage("foobar");
    }
}
