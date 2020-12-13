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
import de.quantummaid.httpmaid.client.websocket.WebsocketCloseHandler;
import de.quantummaid.httpmaid.client.websocket.WebsocketErrorHandler;
import de.quantummaid.httpmaid.client.websocket.WebsocketMessageHandler;
import de.quantummaid.httpmaid.http.HeadersBuilder;
import de.quantummaid.httpmaid.websockets.authorization.AuthorizationDecision;
import de.quantummaid.httpmaid.websockets.endpoint.RawWebsocketAuthorizationBuilder;
import de.quantummaid.httpmaid.websockets.endpoint.RawWebsocketConnectBuilder;
import de.quantummaid.httpmaid.websockets.sender.NonSerializableConnectionInformation;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.List;
import java.util.Map;

import static de.quantummaid.httpmaid.client.websocket.bypass.BypassedConnectionInformation.bypassedConnectionInformation;
import static de.quantummaid.httpmaid.client.websocket.bypass.BypassedWebsocket.bypassedWebsocket;
import static de.quantummaid.httpmaid.util.Validators.validateNotNull;
import static de.quantummaid.httpmaid.websockets.WebsocketMetaDataKeys.ADDITIONAL_WEBSOCKET_DATA;
import static de.quantummaid.httpmaid.websockets.authorization.AuthorizationDecision.AUTHORIZATION_DECISION;
import static de.quantummaid.httpmaid.websockets.endpoint.RawWebsocketAuthorizationBuilder.rawWebsocketAuthorizationBuilder;
import static de.quantummaid.httpmaid.websockets.endpoint.RawWebsocketConnectBuilder.rawWebsocketConnectBuilder;

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
                                           final WebsocketCloseHandler closeHandler,
                                           final WebsocketErrorHandler errorHandler,
                                           final Map<String, List<String>> queryParameters,
                                           final Map<String, List<String>> headers,
                                           final String path) {
        final NonSerializableConnectionInformation connectionInformation = bypassedConnectionInformation(
                messageHandler,
                closeHandler
        );

        final AuthorizationDecision decision = httpMaid.handleRequestSynchronously(
                () -> {
                    final RawWebsocketAuthorizationBuilder builder = rawWebsocketAuthorizationBuilder();
                    builder.withQueryParameterMap(queryParameters);
                    final HeadersBuilder headersBuilder = HeadersBuilder.headersBuilder();
                    headersBuilder.withHeadersMap(headers);
                    builder.withHeaders(headersBuilder.build());
                    return builder.build();
                }, response -> response.metaData().get(AUTHORIZATION_DECISION)
        );

        final Map<String, Object> additionalWebsocketData = decision.additionalData();

        httpMaid.handleRequest(
                () -> {
                    final RawWebsocketConnectBuilder builder = rawWebsocketConnectBuilder();
                    builder.withNonSerializableConnectionInformation(connectionInformation);
                    builder.withQueryParameterMap(queryParameters);
                    final HeadersBuilder headersBuilder = HeadersBuilder.headersBuilder();
                    headersBuilder.withHeadersMap(headers);
                    builder.withHeaders(headersBuilder.build());
                    builder.withAdditionalMetaData(ADDITIONAL_WEBSOCKET_DATA, additionalWebsocketData);
                    return builder.build();
                },
                ignored -> {
                }
        );

        return bypassedWebsocket(httpMaid, messageHandler, connectionInformation);
    }
}
