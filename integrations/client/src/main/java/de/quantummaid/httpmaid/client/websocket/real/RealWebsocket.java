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

package de.quantummaid.httpmaid.client.websocket.real;

import de.quantummaid.httpmaid.client.websocket.Websocket;
import de.quantummaid.httpmaid.client.websocket.WebsocketCloseHandler;
import de.quantummaid.httpmaid.client.websocket.WebsocketErrorHandler;
import de.quantummaid.httpmaid.client.websocket.WebsocketMessageHandler;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketListener;
import org.eclipse.jetty.websocket.client.WebSocketClient;

import java.io.IOException;

import static de.quantummaid.httpmaid.client.HttpMaidClientException.httpMaidClientException;
import static de.quantummaid.httpmaid.client.websocket.real.RealWebsocketState.initialWebsocketState;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class RealWebsocket implements Websocket, WebSocketListener {
    private final RealWebsocketState websocketState = initialWebsocketState();
    private final WebsocketMessageHandler messageHandler;
    private final WebsocketCloseHandler closeHandler;
    private final WebsocketErrorHandler errorHandler;
    private final WebSocketClient client;
    private Session session;

    public static RealWebsocket realWebsocket(final WebsocketMessageHandler messageHandler,
                                              final WebsocketCloseHandler closeHandler,
                                              final WebsocketErrorHandler errorHandler,
                                              final WebSocketClient client) {
        return new RealWebsocket(messageHandler, closeHandler, errorHandler, client);
    }

    void awaitConnect() {
        websocketState.awaitConnect();
    }

    @Override
    public synchronized void send(final String message) {
        if (!websocketState.isConnected()) {
            throw httpMaidClientException("not connected");
        }
        try {
            session.getRemote().sendString(message);
        } catch (final IOException e) {
            throw httpMaidClientException(e);
        }
    }

    @Override
    public synchronized void onWebSocketConnect(final Session session) {
        this.session = session;
        websocketState.setConnected();
    }

    @Override
    public void onWebSocketText(final String message) {
        messageHandler.handle(message);
    }

    @Override
    public void onWebSocketClose(final int statusCode, final String reason) {
        closeHandler.onClose();
    }

    @Override
    public void onWebSocketBinary(final byte[] bytes, final int i, final int i1) {
        throw new UnsupportedOperationException("binary websocket messages are not supported.");
    }

    @Override
    public void onWebSocketError(final Throwable throwable) {
        if (websocketState.isConnected()) {
            errorHandler.onError(throwable);
        } else {
            websocketState.errorOnConnectOccurred(throwable);
        }
    }

    @Override
    public synchronized void close() {
        try {
            client.stop();
        } catch (final Exception e) {
            throw httpMaidClientException(e);
        }
    }
}
