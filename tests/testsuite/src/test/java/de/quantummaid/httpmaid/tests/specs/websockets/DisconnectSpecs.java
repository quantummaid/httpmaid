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
import de.quantummaid.httpmaid.tests.specs.websockets.domain.DisconnectingUseCase;
import de.quantummaid.httpmaid.tests.specs.websockets.domain.MyDisconnector;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static de.quantummaid.httpmaid.HttpMaid.anHttpMaid;
import static de.quantummaid.httpmaid.tests.givenwhenthen.TestEnvironments.ENVIRONMENTS_WITH_ALL_CAPABILITIES;
import static de.quantummaid.httpmaid.tests.givenwhenthen.TestEnvironments.WEBSOCKET_ENVIRONMENTS;

public final class DisconnectSpecs {

    @ParameterizedTest
    @MethodSource(ENVIRONMENTS_WITH_ALL_CAPABILITIES)
    public void disconnectedWebsocketsDoNotCauseProblems(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .post("/broadcast", (request, response) -> request.websockets().sender().sendToAll("foo"))
                        .websocket("check", (request, response) -> response.setBody("websocket has been registered"))
                        .build()
        )
                .when().aWebsocketIsConnected()
                .andWhen().aWebsocketMessageIsSent("{ \"message\": \"check\" }")
                .aWebsocketMessageHasBeenReceivedWithContent("websocket has been registered")

                .andWhen().aWebsocketIsConnected()
                .andWhen().aWebsocketMessageIsSent("{ \"message\": \"check\" }")
                .aWebsocketMessageHasBeenReceivedWithContent("websocket has been registered")

                .andWhen().theLastWebsocketIsDisconnected()

                .andWhen().aRequestToThePath("/broadcast").viaThePostMethod().withTheBody("{ \"message\": \"foo\" }").isIssued()
                .theStatusCodeWas(200)
                .aWebsocketMessageHasBeenReceivedWithContent("foo");
    }

    @ParameterizedTest
    @MethodSource(WEBSOCKET_ENVIRONMENTS)
    public void websocketDisconnectIsReflectedInRuntimeInformation(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .websocket("check", (request, response) -> response.setBody("websocket has been registered"))
                        .build()
        )
                .when().theRuntimeDataIsQueriedUntilTheNumberOfWebsocketsBecomes(0)
                .theQueriedNumberOfWebsocketsIs(0)

                .andWhen().aWebsocketIsConnected()
                .andWhen().aWebsocketMessageIsSent("{ \"message\": \"check\" }")
                .aWebsocketMessageHasBeenReceivedWithContent("websocket has been registered")

                .andWhen().theRuntimeDataIsQueriedUntilTheNumberOfWebsocketsBecomes(1)
                .theQueriedNumberOfWebsocketsIs(1)

                .andWhen().theLastWebsocketIsDisconnected()
                .andWhen().theRuntimeDataIsQueriedUntilTheNumberOfWebsocketsBecomes(0)
                .theQueriedNumberOfWebsocketsIs(0);
    }

    @ParameterizedTest
    @MethodSource(WEBSOCKET_ENVIRONMENTS)
    public void handlersCanDisconnectWebsockets(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .websocket("disconnect", (request, response) -> request.websockets().disconnector().disconnectAll())
                        .websocket("check", (request, response) -> response.setBody("websocket has been registered"))
                        .build()
        )
                .when().theRuntimeDataIsQueriedUntilTheNumberOfWebsocketsBecomes(0)
                .theQueriedNumberOfWebsocketsIs(0)

                .andWhen().aWebsocketIsConnected()
                .andWhen().aWebsocketMessageIsSent("{ \"message\": \"check\" }")
                .aWebsocketMessageHasBeenReceivedWithContent("websocket has been registered")

                .andWhen().theRuntimeDataIsQueriedUntilTheNumberOfWebsocketsBecomes(1)
                .theQueriedNumberOfWebsocketsIs(1)

                .andWhen().aWebsocketMessageIsSent("{ \"message\": \"disconnect\" }")

                .andWhen().theRuntimeDataIsQueriedUntilTheNumberOfWebsocketsBecomes(0)
                .theQueriedNumberOfWebsocketsIs(0)
                .allWebsocketsHaveBeenClosed();
    }

    @ParameterizedTest
    @MethodSource(WEBSOCKET_ENVIRONMENTS)
    public void useCasesCanDisconnectWebsockets(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .websocket("disconnect", DisconnectingUseCase.class)
                        .websocket("check", (request, response) -> response.setBody("websocket has been registered"))
                        .disconnectWebsocketsUsing(MyDisconnector.class, sender -> sender::disconnectAll)
                        .build()
        )
                .when().theRuntimeDataIsQueriedUntilTheNumberOfWebsocketsBecomes(0)
                .theQueriedNumberOfWebsocketsIs(0)

                .andWhen().aWebsocketIsConnected()
                .andWhen().aWebsocketMessageIsSent("{ \"message\": \"check\" }")
                .aWebsocketMessageHasBeenReceivedWithContent("websocket has been registered")

                .andWhen().theRuntimeDataIsQueriedUntilTheNumberOfWebsocketsBecomes(1)
                .theQueriedNumberOfWebsocketsIs(1)

                .andWhen().aWebsocketMessageIsSent("{ \"message\": \"disconnect\" }")

                .andWhen().theRuntimeDataIsQueriedUntilTheNumberOfWebsocketsBecomes(0)
                .theQueriedNumberOfWebsocketsIs(0)
                .allWebsocketsHaveBeenClosed();
    }
}
