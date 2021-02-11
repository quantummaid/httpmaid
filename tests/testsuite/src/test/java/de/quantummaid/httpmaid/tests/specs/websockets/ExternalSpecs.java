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

import de.quantummaid.httpmaid.HttpMaid;
import de.quantummaid.httpmaid.tests.givenwhenthen.TestEnvironment;
import de.quantummaid.httpmaid.tests.specs.websockets.domain.MyMessage;
import de.quantummaid.httpmaid.tests.specs.websockets.domain.MySerializingBroadcaster;
import de.quantummaid.httpmaid.websockets.broadcast.SerializingSender;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Map;

import static de.quantummaid.httpmaid.HttpMaid.anHttpMaid;
import static de.quantummaid.httpmaid.tests.givenwhenthen.TestEnvironments.WEBSOCKET_ENVIRONMENTS;

public final class ExternalSpecs {

    @ParameterizedTest
    @MethodSource(WEBSOCKET_ENVIRONMENTS)
    public void websocketSenderCanBeUsedFromOutsideHttpMaid(final TestEnvironment testEnvironment) {
        final HttpMaid httpMaid = anHttpMaid()
                .websocket((request, response) -> response.setBody("pong"))
                .broadcastToWebsocketsUsing(MySerializingBroadcaster.class, MyMessage.class, sender -> message -> {
                    throw new UnsupportedOperationException("this does not matter");
                })
                .build();
        final SerializingSender<MyMessage> sender = httpMaid.websocketSender(MyMessage.class);
        testEnvironment.given(httpMaid)
                .when().aWebsocketIsConnected()
                .andWhen().aWebsocketMessageIsSent("ping")
                .oneWebsocketHasReceivedTheMessage("pong")
                .andWhen().running(() -> sender.sendToAll(new MyMessage("a", "b", "c")))
                .allWebsocketsHaveReceivedTheJsonMessage(Map.of(
                        "field1", "a",
                        "field2", "b",
                        "field3", "c"
                ));
    }
}
