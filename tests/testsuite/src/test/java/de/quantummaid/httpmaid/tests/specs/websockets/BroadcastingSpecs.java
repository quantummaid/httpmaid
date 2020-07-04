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

import de.quantummaid.httpmaid.tests.givenwhenthen.TestEnvironment;
import de.quantummaid.httpmaid.tests.specs.websockets.domain.BroadcastingUseCase;
import de.quantummaid.httpmaid.tests.specs.websockets.domain.MyBroadcaster;
import de.quantummaid.httpmaid.websockets.criteria.WebsocketCriteria;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Map;

import static de.quantummaid.httpmaid.HttpMaid.anHttpMaid;
import static de.quantummaid.httpmaid.tests.givenwhenthen.TestEnvironments.ENVIRONMENTS_WITH_ALL_CAPABILITIES;

public final class BroadcastingSpecs {

    @ParameterizedTest
    @MethodSource(ENVIRONMENTS_WITH_ALL_CAPABILITIES)
    public void handlersCanBroadcast(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .post("/broadcast", (request, response) -> request.websockets().sender().sendToAll("foo"))
                        .websocket("check", (request, response) -> response.setBody("websocket has been registered"))
                        .build()
        )
                .when().aWebsocketIsConnected()
                .andWhen().aWebsocketMessageIsSent("{ \"message\": \"check\" }")
                .allWebsocketsHaveReceivedTheMessage("websocket has been registered")
                .andWhen().aRequestToThePath("/broadcast").viaThePostMethod().withTheBody("{ \"message\": \"foo\" }").isIssued()
                .theStatusCodeWas(200)
                .allWebsocketsHaveReceivedTheMessage("foo");
    }

    @ParameterizedTest
    @MethodSource(ENVIRONMENTS_WITH_ALL_CAPABILITIES)
    public void handlersCanBroadcastMultipleMessages(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .post("/broadcast/<message>", (request, response) -> {
                            final String message = request.pathParameters().getPathParameter("message");
                            request.websockets().sender().sendToAll(message);
                        })
                        .websocket("check", (request, response) -> response.setBody("websocket has been registered"))
                        .build()
        )
                .when().aWebsocketIsConnected()
                .andWhen().aWebsocketMessageIsSent("{ \"message\": \"check\" }")
                .allWebsocketsHaveReceivedTheMessage("websocket has been registered")

                .andWhen().aRequestToThePath("/broadcast/value1").viaThePostMethod().withAnEmptyBody().isIssued()
                .theStatusCodeWas(200)
                .andWhen().aRequestToThePath("/broadcast/value2").viaThePostMethod().withAnEmptyBody().isIssued()
                .theStatusCodeWas(200)
                .andWhen().aRequestToThePath("/broadcast/value3").viaThePostMethod().withAnEmptyBody().isIssued()
                .theStatusCodeWas(200)

                .allWebsocketsHaveReceivedTheMessage("value1")
                .allWebsocketsHaveReceivedTheMessage("value2")
                .allWebsocketsHaveReceivedTheMessage("value3");
    }

    @ParameterizedTest
    @MethodSource(ENVIRONMENTS_WITH_ALL_CAPABILITIES)
    public void handlersCanBroadcastToWebsocketsWithASpecificHeader(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .post("/broadcast", (request, response) -> request.websockets().sender().sendToAll("foo"))
                        .post("/broadcast_header", (request, response) -> request.websockets().sender().sendTo("bar",
                                WebsocketCriteria.websocketCriteria()
                                        .header("X-My-Header", "foo")
                                )
                        )
                        .websocket("check", (request, response) -> response.setBody("websocket has been registered"))
                        .build()
        )
                .when().aWebsocketIsConnected(Map.of(), Map.of("X-My-Header", List.of("foo")))

                .andWhen().aWebsocketMessageIsSent("{ \"message\": \"check\" }")
                .andWhen().aWebsocketIsConnected(Map.of(), Map.of("X-My-Header", List.of("bar")))
                .andWhen().aWebsocketMessageIsSent("{ \"message\": \"check\" }")
                .andWhen().aWebsocketIsConnected(Map.of(), Map.of("X-My-Header", List.of("bar")))
                .andWhen().aWebsocketMessageIsSent("{ \"message\": \"check\" }")

                .allWebsocketsHaveReceivedTheMessage("websocket has been registered")

                .andWhen().aRequestToThePath("/broadcast_header").viaThePostMethod().withAnEmptyBody().isIssued()
                .theStatusCodeWas(200)
                .andWhen().aRequestToThePath("/broadcast").viaThePostMethod().withAnEmptyBody().isIssued()
                .theStatusCodeWas(200)

                .allWebsocketsHaveReceivedTheMessage("foo")
                .oneWebsocketHasReceivedTheMessage("bar");
    }

    @ParameterizedTest
    @MethodSource(ENVIRONMENTS_WITH_ALL_CAPABILITIES)
    public void useCasesCanBroadcast(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .post("/broadcast", BroadcastingUseCase.class)
                        .websocket("check", (request, response) -> response.setBody("websocket has been registered"))
                        .broadcastToWebsocketsUsing(MyBroadcaster.class, String.class, sender -> sender::sendToAll)
                        .build()
        )
                .when().aWebsocketIsConnected()
                .andWhen().aWebsocketMessageIsSent("{ \"message\": \"check\" }")
                .allWebsocketsHaveReceivedTheMessage("websocket has been registered")
                .andWhen().aRequestToThePath("/broadcast").viaThePostMethod().withTheBody("{ \"message\": \"foo\" }").isIssued()
                .theStatusCodeWas(200)
                .allWebsocketsHaveReceivedTheMessage("foo");
    }
}
