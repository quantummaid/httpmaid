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

package de.quantummaid.httpmaid.tests.deployers.fakeawslambda.websocket;

import de.quantummaid.httpmaid.tests.deployers.fakeawslambda.ValidatedAwsLambdaEndpoint;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketListener;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;

import static de.quantummaid.httpmaid.tests.deployers.fakeawslambda.websocket.EventUtils.*;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class FakeLambdaWebsocket implements WebSocketListener {
    private final ValidatedAwsLambdaEndpoint endpoint;
    private final String connectionId;
    private Session session;
    private final Map<String, Object> authorizerContext;

    public static FakeLambdaWebsocket fakeLambdaWebsocket(final ValidatedAwsLambdaEndpoint endpoint,
                                                          final String connectionId,
                                                          final Map<String, Object> authorizerContext) {
        return new FakeLambdaWebsocket(endpoint, connectionId, authorizerContext);
    }

    @Override
    public synchronized void onWebSocketText(final String message) {
        final Map<String, Object> event = createWebsocketMessageEvent(connectionId, message, authorizerContext);
        final Map<String, Object> responseEvent = endpoint.delegate(event);
        final String body = (String) responseEvent.get("body");
        if (body != null) {
            send(body);
        }
    }

    public void send(final String message) {
        try {
            session.getRemote().sendString(message);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onWebSocketBinary(final byte[] bytes, final int i, final int i1) {

    }

    @Override
    public void onWebSocketClose(final int i, final String s) {
        final Map<String, Object> event = createWebsocketDisconnectEvent(connectionId, authorizerContext);
        endpoint.delegate(event);
    }

    @Override
    public synchronized void onWebSocketConnect(final Session session) {
        this.session = session;
        final Map<String, Object> event = createWebsocketConnectEvent(connectionId, session.getUpgradeRequest(), authorizerContext);
        endpoint.delegate(event);
    }

    @Override
    public void onWebSocketError(final Throwable throwable) {
        throwable.printStackTrace();
        disconnect();
    }

    public void disconnect() {
        try {
            session.disconnect();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
