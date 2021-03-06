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

package de.quantummaid.httpmaid.jsr356;

import de.quantummaid.httpmaid.HttpMaid;
import de.quantummaid.httpmaid.chains.MetaData;
import de.quantummaid.httpmaid.http.Headers;
import de.quantummaid.httpmaid.http.HeadersBuilder;
import de.quantummaid.httpmaid.websockets.authorization.AuthorizationDecision;
import de.quantummaid.httpmaid.websockets.endpoint.RawWebsocketAuthorizationBuilder;
import de.quantummaid.httpmaid.websockets.registry.WebsocketRegistryEntry;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;
import java.util.List;
import java.util.Map;

import static de.quantummaid.httpmaid.jsr356.Jsr356Endpoint.programmaticJsr356Endpoint;
import static de.quantummaid.httpmaid.jsr356.Jsr356Exception.jsr356Exception;
import static de.quantummaid.httpmaid.websockets.WebsocketMetaDataKeys.WEBSOCKET_REGISTRY_ENTRY;
import static de.quantummaid.httpmaid.websockets.authorization.AuthorizationDecision.AUTHORIZATION_DECISION;
import static de.quantummaid.httpmaid.websockets.endpoint.RawWebsocketAuthorizationBuilder.rawWebsocketAuthorizationBuilder;
import static de.quantummaid.httpmaid.websockets.sender.NonSerializableWebsocketSender.NON_SERIALIZABLE_WEBSOCKET_SENDER;

@ToString
@EqualsAndHashCode(callSuper = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Jsr356EndpointConfigurator extends ServerEndpointConfig.Configurator {
    private final HttpMaid httpMaid;
    private final HandshakeMetaData handshakeMetaData;

    public static ServerEndpointConfig.Configurator jsr356EndpointConfigurator(final HttpMaid httpMaid,
                                                                               final HandshakeMetaData handshakeMetaData) {
        return new Jsr356EndpointConfigurator(httpMaid, handshakeMetaData);
    }

    @SuppressWarnings("unchecked")
    @Override
    public synchronized <T> T getEndpointInstance(final Class<T> endpointClass) {
        final WebsocketRegistryEntry websocketRegistryEntry = handshakeMetaData.getWebsocketRegistryEntry();
        return (T) programmaticJsr356Endpoint(httpMaid, websocketRegistryEntry);
    }

    @Override
    public synchronized void modifyHandshake(final ServerEndpointConfig serverEndpointConfig,
                                             final HandshakeRequest request,
                                             final HandshakeResponse response) {
        final Map<String, List<String>> requestHeaders = request.getHeaders();
        final HeadersBuilder headersBuilder = HeadersBuilder.headersBuilder();
        headersBuilder.withHeadersMap(requestHeaders);
        final Headers headers = headersBuilder.build();

        final AuthorizationDecision authorizationDecision = httpMaid.handleRequestSynchronously(
                () -> {
                    final RawWebsocketAuthorizationBuilder builder = rawWebsocketAuthorizationBuilder(NON_SERIALIZABLE_WEBSOCKET_SENDER);
                    builder.withHeaders(headers);
                    final String queryString = request.getQueryString();
                    builder.withEncodedQueryString(queryString);
                    return builder.build();
                },
                authorizationResponse -> {
                    final MetaData metaData = authorizationResponse.metaData();
                    handshakeMetaData.setWebsocketRegistryEntry(metaData.get(WEBSOCKET_REGISTRY_ENTRY));
                    return authorizationResponse.metaData().get(AUTHORIZATION_DECISION);
                }
        );

        if (!authorizationDecision.isAuthorized()) {
            throw jsr356Exception("not authorized");
        }
    }
}
