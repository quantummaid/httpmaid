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

package de.quantummaid.httpmaid.tests.specs.websockets;

import de.quantummaid.httpmaid.http.headers.ContentType;
import de.quantummaid.httpmaid.tests.givenwhenthen.TestEnvironment;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Map;

import static de.quantummaid.httpmaid.HttpMaid.anHttpMaid;
import static de.quantummaid.httpmaid.tests.givenwhenthen.TestEnvironments.WEBSOCKET_ENVIRONMENTS;
import static de.quantummaid.httpmaid.tests.givenwhenthen.TestEnvironments.WEBSOCKET_ENVIRONMENTS_WITHOUT_SHITTY_CLIENT;

public final class WebsocketHeaderSpecs {

    @ParameterizedTest
    @MethodSource(WEBSOCKET_ENVIRONMENTS)
    public void websocketsHandlerCanAccessHeaders(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .websocket("handler", (request, response) -> {
                            final String myHeader = request.headers().header("myHeader");
                            response.setBody(myHeader);
                        })
                        .build()
        )
                .when().aWebsocketIsConnected(Map.of(), Map.of("myHeader", List.of("foo")))
                .andWhen().aWebsocketMessageIsSent("{ \"message\": \"handler\" }")
                .aWebsocketMessageHasBeenReceivedWithContent("foo");
    }

    @ParameterizedTest
    @MethodSource(WEBSOCKET_ENVIRONMENTS)
    public void websocketHandlerCanAccessContentType(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .websocket("handler", (request, response) -> {
                            final ContentType contentType = request.contentType();
                            response.setBody(contentType.internalValueForMapping());
                        })
                        .build()
        )
                .when().aWebsocketIsConnected(Map.of(), Map.of("Content-Type", List.of("application/json")))
                .andWhen().aWebsocketMessageIsSent("{ \"message\": \"handler\" }")
                .aWebsocketMessageHasBeenReceivedWithContent("application/json");
    }

    @ParameterizedTest
    @MethodSource(WEBSOCKET_ENVIRONMENTS_WITHOUT_SHITTY_CLIENT)
    public void websocketHandlerCanAccessRequestHeaderThatOccursMultipleTimesWithDifferentValues(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .websocket("handler", (request, response) -> {
                            final List<String> map = request.headers().allValuesFor("X-Headername");
                            response.setBody(Map.of("headers", map));
                        })
                        .build()
        )
                .when().aWebsocketIsConnected(Map.of(), Map.of("X-Headername", List.of("value1", "value2")))
                .andWhen().aWebsocketMessageIsSent("{ \"message\": \"handler\" }")
                .aWebsocketMessageHasBeenReceivedWithContent("{\"headers\":[\"value1\",\"value2\"]}");
    }
}
