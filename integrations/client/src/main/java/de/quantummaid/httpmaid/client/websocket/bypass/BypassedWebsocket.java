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

package de.quantummaid.httpmaid.client.websocket.bypass;

import de.quantummaid.httpmaid.HttpMaid;
import de.quantummaid.httpmaid.client.websocket.Websocket;
import de.quantummaid.httpmaid.client.websocket.WebsocketMessageHandler;
import de.quantummaid.httpmaid.websockets.sender.NonSerializableConnectionInformation;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import static de.quantummaid.httpmaid.websockets.endpoint.RawWebsocketDisconnect.rawWebsocketDisconnect;
import static de.quantummaid.httpmaid.websockets.endpoint.RawWebsocketMessage.rawWebsocketMessage;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class BypassedWebsocket implements Websocket {
    private final HttpMaid httpMaid;
    private final WebsocketMessageHandler messageHandler;
    private final NonSerializableConnectionInformation connectionInformation;

    public static BypassedWebsocket bypassedWebsocket(final HttpMaid httpMaid,
                                                      final WebsocketMessageHandler messageHandler,
                                                      final NonSerializableConnectionInformation connectionInformation) {
        return new BypassedWebsocket(httpMaid, messageHandler, connectionInformation);
    }

    public void send(final String message) {
        httpMaid.handleRequest(
                () -> rawWebsocketMessage(connectionInformation, message),
                response -> response.optionalStringBody()
                        .ifPresent(messageHandler::handle)
        );
    }

    @Override
    public void close() {
        httpMaid.handleRequest(
                () -> rawWebsocketDisconnect(connectionInformation),
                response -> {
                }
        );
    }
}
