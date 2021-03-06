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

package de.quantummaid.httpmaid.jetty;

import de.quantummaid.httpmaid.HttpMaid;
import de.quantummaid.httpmaid.websockets.endpoint.RawWebsocketConnectBuilder;
import de.quantummaid.httpmaid.websockets.registry.WebsocketRegistryEntry;
import de.quantummaid.httpmaid.websockets.sender.NonSerializableConnectionInformation;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketListener;

import static de.quantummaid.httpmaid.jetty.JettyConnectionInformation.jettyConnectionInformation;
import static de.quantummaid.httpmaid.websockets.endpoint.RawWebsocketConnect.rawWebsocketConnectBuilder;
import static de.quantummaid.httpmaid.websockets.endpoint.RawWebsocketDisconnect.rawWebsocketDisconnect;
import static de.quantummaid.httpmaid.websockets.endpoint.RawWebsocketMessage.rawWebsocketMessage;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class JettyHttpMaidWebsocket implements WebSocketListener {
    private final HttpMaid httpMaid;
    private final WebsocketRegistryEntry websocketRegistryEntry;
    private NonSerializableConnectionInformation connectionInformation;

    public static JettyHttpMaidWebsocket jettyHttpMaidWebsocket(final HttpMaid endpoint,
                                                                final WebsocketRegistryEntry websocketRegistryEntry) {
        return new JettyHttpMaidWebsocket(endpoint, websocketRegistryEntry);
    }

    @Override
    public synchronized void onWebSocketConnect(final Session session) {
        this.connectionInformation = jettyConnectionInformation(session);
        httpMaid.handleRequest(() -> {
            final RawWebsocketConnectBuilder builder = rawWebsocketConnectBuilder();
            builder.withNonSerializableConnectionInformation(connectionInformation);
            builder.withRegistryEntry(websocketRegistryEntry);
            return builder.build();
        }, ignored -> {
        });
    }

    @Override
    public synchronized void onWebSocketText(final String message) {
        httpMaid.handleRequest(
                () -> rawWebsocketMessage(connectionInformation, message),
                response -> response.optionalStringBody()
                        .ifPresent(connectionInformation::send)
        );
    }

    @Override
    public void onWebSocketClose(final int i, final String s) {
        httpMaid.handleRequest(
                () -> rawWebsocketDisconnect(connectionInformation),
                response -> {
                });
    }

    @Override
    public void onWebSocketError(final Throwable throwable) {
        log.info("error in websocket", throwable);
    }

    @Override
    public void onWebSocketBinary(final byte[] bytes, final int i, final int i1) {
        final String message = "binary websocket messages are not supported";
        log.info(message);
        throw new UnsupportedOperationException(message);
    }
}
