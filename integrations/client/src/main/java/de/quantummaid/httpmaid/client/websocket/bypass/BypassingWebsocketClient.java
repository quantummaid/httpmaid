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
import de.quantummaid.httpmaid.client.websocket.WebsocketClient;
import de.quantummaid.httpmaid.client.websocket.WebsocketMessageHandler;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.List;
import java.util.Map;

import static de.quantummaid.httpmaid.client.websocket.bypass.BypassedWebsocket.bypassedWebsocket;
import static de.quantummaid.httpmaid.util.Validators.validateNotNull;
import static de.quantummaid.httpmaid.websockets.endpoint.RawWebsocketConnect.rawWebsocketConnect;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class BypassingWebsocketClient implements WebsocketClient {
    private final HttpMaid httpMaid;

    public static BypassingWebsocketClient bypassingWebsocketClient(final HttpMaid httpMaid) {
        validateNotNull(httpMaid, "httpMaid");
        return new BypassingWebsocketClient(httpMaid);
    }

    @Override
    public BypassedWebsocket openWebsocket(final WebsocketMessageHandler messageHandler,
                                           final Map<String, String> queryParameters,
                                           final Map<String, List<String>> headers,
                                           final String path) {
        httpMaid.handleRequest(
                () -> rawWebsocketConnect(messageHandler, queryParameters, headers),
                response -> {
                }
        );
        return bypassedWebsocket(httpMaid, messageHandler);
    }
}
