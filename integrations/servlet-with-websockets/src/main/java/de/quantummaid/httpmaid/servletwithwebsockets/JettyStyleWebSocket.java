/*
 * Copyright (c) 2019 Richard Hauswald - https://quantummaid.de/.
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

package de.quantummaid.httpmaid.servletwithwebsockets;

import de.quantummaid.httpmaid.HttpMaid;
import de.quantummaid.httpmaid.chains.MetaData;
import de.quantummaid.httpmaid.websockets.WebSocketDelegate;
import de.quantummaid.httpmaid.websockets.registry.WebSocketId;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketListener;

import java.io.IOException;

import static de.quantummaid.httpmaid.HttpMaidChainKeys.REQUEST_BODY_STREAM;
import static de.quantummaid.httpmaid.chains.MetaData.emptyMetaData;
import static de.quantummaid.httpmaid.util.Streams.stringToInputStream;
import static de.quantummaid.httpmaid.util.Validators.validateNotNull;
import static de.quantummaid.httpmaid.websockets.WebsocketChainKeys.IS_WEBSOCKET_MESSAGE;
import static de.quantummaid.httpmaid.websockets.WebsocketChainKeys.WEBSOCKET_ID;
import static de.quantummaid.httpmaid.websockets.WebsocketChains.WEBSOCKET_CLOSED;
import static de.quantummaid.httpmaid.websockets.WebsocketChains.WEBSOCKET_OPEN;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class JettyStyleWebSocket implements WebSocketListener, WebSocketDelegate {
    private final HttpMaid httpMaid;
    private final WebSocketId id;
    private Session session;

    static JettyStyleWebSocket jettyStyleSocket(final HttpMaid httpMaid,
                                                final WebSocketId webSocketId) {
        validateNotNull(httpMaid, "httpMaid");
        validateNotNull(webSocketId, "webSocketId");
        return new JettyStyleWebSocket(httpMaid, webSocketId);
    }

    @Override
    public void sendText(final String text) {
        final RemoteEndpoint remote = session.getRemote();
        try {
            remote.sendString(text);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        this.session.close();
    }

    @Override
    public void onWebSocketBinary(final byte[] bytes, final int i, final int i1) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onWebSocketText(final String text) {
        final MetaData metaData = emptyMetaData();
        metaData.set(WEBSOCKET_ID, id);
        metaData.set(REQUEST_BODY_STREAM, stringToInputStream(text));
        metaData.set(IS_WEBSOCKET_MESSAGE, true);
        httpMaid.handleRequest(metaData, m -> {
        });
    }

    @Override
    public void onWebSocketClose(final int i, final String s) {
        final MetaData metaData = emptyMetaData();
        metaData.set(WEBSOCKET_ID, id);
        httpMaid.handle(WEBSOCKET_CLOSED, metaData);
    }

    @Override
    public void onWebSocketConnect(final Session session) {
        this.session = session;
        final MetaData metaData = emptyMetaData();
        metaData.set(WEBSOCKET_ID, id);
        httpMaid.handle(WEBSOCKET_OPEN, metaData);
    }

    @Override
    public void onWebSocketError(final Throwable throwable) {
    }
}
