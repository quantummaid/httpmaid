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
import de.quantummaid.httpmaid.tests.specs.websockets.domain.TestUseCase;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Map;

import static de.quantummaid.httpmaid.HttpMaid.anHttpMaid;
import static de.quantummaid.httpmaid.tests.givenwhenthen.TestEnvironments.WEBSOCKET_ENVIRONMENTS;
import static de.quantummaid.httpmaid.websockets.WebsocketConfigurators.toSelectWebsocketRoutesBasedOn;

public final class WebsocketSpecs {

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
    public void websocketsCanAccessHeaders(final TestEnvironment testEnvironment) {
        testEnvironment.given(checkpoints ->
                anHttpMaid()
                        .websocket("handler", (request, response) -> {
                            final String myHeader = request.headers().header("myHeader");
                            checkpoints.visitCheckpoint(myHeader);
                        })
                        .build()
        )
                .when().aWebsocketIsConnected(Map.of(), Map.of("myHeader", List.of("foo")))
                .andWhen().aWebsocketMessageIsSent("{ \"message\": \"handler\" }")
                .theCheckpointHasBeenVisited("foo");
    }

    @ParameterizedTest
    @MethodSource(WEBSOCKET_ENVIRONMENTS)
    public void websocketsCanHaveContentType(final TestEnvironment testEnvironment) {
        testEnvironment.given(checkpoints ->
                anHttpMaid()
                        .websocket("handler", (request, response) -> {
                            final ContentType contentType = request.contentType();
                            checkpoints.visitCheckpoint(contentType.internalValueForMapping());
                        })
                        .build()
        )
                .when().aWebsocketIsConnected(Map.of(), Map.of("Content-Type", List.of("application/json")))
                .andWhen().aWebsocketMessageIsSent("{ \"message\": \"handler\" }")
                .theCheckpointHasBeenVisited("application/json");
    }

    @ParameterizedTest
    @MethodSource(WEBSOCKET_ENVIRONMENTS)
    public void websocketsAccessQueryParameters(final TestEnvironment testEnvironment) {
        testEnvironment.given(checkpoints ->
                anHttpMaid()
                        .websocket("handler", (request, response) -> {
                            final String queryParameter = request.queryParameters().getQueryParameter("foo");
                            checkpoints.visitCheckpoint(queryParameter);
                        })
                        .build()
        )
                .when().aWebsocketIsConnected(Map.of("foo", "bar"), Map.of())
                .andWhen().aWebsocketMessageIsSent("{ \"message\": \"handler\" }")
                .theCheckpointHasBeenVisited("bar");
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
                .aWebsocketMessageHasBeenReceivedWithContent("foo");
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
                .aWebsocketMessageHasBeenReceivedWithContent("\"foobar\"");
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
                .aWebsocketMessageHasBeenReceivedWithContent("correct handler");
    }
}
