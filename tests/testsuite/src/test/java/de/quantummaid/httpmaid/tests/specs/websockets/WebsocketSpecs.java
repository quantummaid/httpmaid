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
import de.quantummaid.httpmaid.tests.specs.websockets.domain.TestUseCase;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static de.quantummaid.httpmaid.HttpMaid.anHttpMaid;
import static de.quantummaid.httpmaid.tests.givenwhenthen.TestEnvironments.WEBSOCKET_ENVIRONMENTS;
import static de.quantummaid.httpmaid.websockets.WebsocketConfigurators.toSelectWebsocketRoutesBasedOn;

public final class WebsocketSpecs {

    @ParameterizedTest
    @MethodSource(WEBSOCKET_ENVIRONMENTS)
    public void lowLevelWebsocketHandlersCanBeRegisteredAsCatchAll(final TestEnvironment testEnvironment) {
        testEnvironment.given(checkpoints ->
                anHttpMaid()
                        .websocket((request, response) -> checkpoints.visitCheckpoint("test"))
                        .build())
                .when().aWebsocketIsConnected()
                .andWhen().aWebsocketMessageIsSent("foooo")
                .theCheckpointHasBeenVisited("test");
    }

    @ParameterizedTest
    @MethodSource(WEBSOCKET_ENVIRONMENTS)
    public void websocketsCanBeReceivedWithLowLevelHandler(final TestEnvironment testEnvironment) {
        testEnvironment.given(checkpoints ->
                anHttpMaid()
                        .websocket("test", (request, response) -> checkpoints.visitCheckpoint("test"))
                        .build()
        )
                .when().aWebsocketIsConnected()
                .andWhen().aWebsocketMessageIsSent("{ \"message\": \"test\" }")
                .theCheckpointHasBeenVisited("test");
    }

    @ParameterizedTest
    @MethodSource(WEBSOCKET_ENVIRONMENTS)
    public void websocketsCanBeMultiplexed(final TestEnvironment testEnvironment) {
        testEnvironment.given(checkpoints ->
                anHttpMaid()
                        .websocket("handler1", (request, response) -> checkpoints.visitCheckpoint("handler 1"))
                        .websocket("handler2", (request, response) -> checkpoints.visitCheckpoint("handler 2"))
                        .build()
        )
                .when().aWebsocketIsConnected()
                .andWhen().aWebsocketMessageIsSent("{ \"message\": \"handler2\" }")
                .theCheckpointHasBeenVisited("handler 2");
    }

    @ParameterizedTest
    @MethodSource(WEBSOCKET_ENVIRONMENTS)
    public void handlerResponseIsSentBackOnWebsocket(final TestEnvironment testEnvironment) {
        testEnvironment.given(checkpoints ->
                anHttpMaid()
                        .websocket("handler", (request, response) -> response.setBody("foo"))
                        .build()
        )
                .when().aWebsocketIsConnected()
                .andWhen().aWebsocketMessageIsSent("{ \"message\": \"handler\" }")
                .allWebsocketsHaveReceivedTheMessage("foo");
    }

    @ParameterizedTest
    @MethodSource(WEBSOCKET_ENVIRONMENTS)
    public void usecaseCanBeBoundToCatchAllWebsocket(final TestEnvironment testEnvironment) {
        testEnvironment.given(checkpoints ->
                anHttpMaid()
                        .websocket(TestUseCase.class)
                        .build()
        )
                .when().aWebsocketIsConnected()
                .andWhen().aWebsocketMessageIsSent("" +
                "{\n" +
                "   \"parameter1\": \"foo\",\n" +
                "   \"parameter2\": \"bar\"\n" +
                "}")
                .allWebsocketsHaveReceivedTheMessage("\"foobar\"");
    }

    @ParameterizedTest
    @MethodSource(WEBSOCKET_ENVIRONMENTS)
    public void usecaseCanBeBoundToWebsocket(final TestEnvironment testEnvironment) {
        testEnvironment.given(checkpoints ->
                anHttpMaid()
                        .websocket("handler", TestUseCase.class)
                        .build()
        )
                .when().aWebsocketIsConnected()
                .andWhen().aWebsocketMessageIsSent("" +
                "{\n" +
                "   \"message\": \"handler\",\n" +
                "   \"parameter1\": \"foo\",\n" +
                "   \"parameter2\": \"bar\"\n" +
                "}")
                .allWebsocketsHaveReceivedTheMessage("\"foobar\"");
    }

    @ParameterizedTest
    @MethodSource(WEBSOCKET_ENVIRONMENTS)
    public void defaultRouteSelectionExpressionCanBeOverwritten(final TestEnvironment testEnvironment) {
        testEnvironment.given(checkpoints ->
                anHttpMaid()
                        .websocket("handler1", (request, response) -> response.setBody("wrong handler"))
                        .websocket("handler2", (request, response) -> response.setBody("correct handler"))
                        .configured(toSelectWebsocketRoutesBasedOn("action"))
                        .build()
        )
                .when().aWebsocketIsConnected()
                .andWhen().aWebsocketMessageIsSent("" +
                "{\n" +
                "   \"message\": \"handler1\",\n" +
                "   \"action\": \"handler2\"\n" +
                "}")
                .allWebsocketsHaveReceivedTheMessage("correct handler");
    }
}
