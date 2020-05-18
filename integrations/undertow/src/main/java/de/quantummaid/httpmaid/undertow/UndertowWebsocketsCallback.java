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

package de.quantummaid.httpmaid.undertow;

import de.quantummaid.httpmaid.HttpMaid;
import de.quantummaid.httpmaid.websockets.endpoint.RawWebsocketConnectBuilder;
import de.quantummaid.httpmaid.websockets.sender.NonSerializableConnectionInformation;
import io.undertow.websockets.WebSocketConnectionCallback;
import io.undertow.websockets.core.WebSocketChannel;
import io.undertow.websockets.spi.WebSocketHttpExchange;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import static de.quantummaid.httpmaid.undertow.ReceiveListener.receiveListener;
import static de.quantummaid.httpmaid.websockets.endpoint.RawWebsocketConnect.rawWebsocketConnectBuilder;
import static io.undertow.websockets.core.WebSockets.sendText;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class UndertowWebsocketsCallback implements WebSocketConnectionCallback {
    private final HttpMaid httpMaid;

    public static UndertowWebsocketsCallback undertowWebsocketsCallback(final HttpMaid httpMaid) {
        return new UndertowWebsocketsCallback(httpMaid);
    }

    @Override
    public void onConnect(final WebSocketHttpExchange exchange, final WebSocketChannel channel) {
        final NonSerializableConnectionInformation connectionInformation = message -> sendText(message, channel, null);
        httpMaid.handleRequest(() -> {
            final RawWebsocketConnectBuilder builder = rawWebsocketConnectBuilder();
            builder.withNonSerializableConnectionInformation(connectionInformation);
            builder.withEncodedQueryParameters(exchange.getQueryString());
            builder.withHeaders(exchange.getRequestHeaders());
            return builder.build();
        }, response -> {
        });
        channel.getReceiveSetter().set(receiveListener(connectionInformation, httpMaid));
        channel.resumeReceives();
    }
}
