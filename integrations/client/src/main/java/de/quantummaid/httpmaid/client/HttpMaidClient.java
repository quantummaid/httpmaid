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

package de.quantummaid.httpmaid.client;

import de.quantummaid.httpmaid.HttpMaid;
import de.quantummaid.httpmaid.client.clientbuilder.PortStage;
import de.quantummaid.httpmaid.client.issuer.Issuer;
import de.quantummaid.httpmaid.client.issuer.real.Protocol;
import de.quantummaid.httpmaid.client.issuer.real.RealIssuer;
import de.quantummaid.httpmaid.client.websocket.Websocket;
import de.quantummaid.httpmaid.client.websocket.WebsocketClient;
import de.quantummaid.httpmaid.client.websocket.WebsocketMessageHandler;
import de.quantummaid.httpmaid.client.websocket.bypass.BypassingWebsocketClient;
import de.quantummaid.httpmaid.filtermap.FilterMap;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

import static de.quantummaid.httpmaid.client.HttpMaidClientBuilder.clientBuilder;
import static de.quantummaid.httpmaid.client.issuer.bypass.BypassIssuer.bypassIssuer;
import static de.quantummaid.httpmaid.client.issuer.real.RealIssuer.realIssuer;
import static de.quantummaid.httpmaid.client.websocket.bypass.BypassingWebsocketClient.bypassingWebsocketClient;
import static de.quantummaid.httpmaid.client.websocket.real.RealWebsocketClient.realWebsocketClient;
import static de.quantummaid.httpmaid.util.Validators.validateNotNull;
import static de.quantummaid.httpmaid.util.Validators.validateNotNullNorEmpty;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class HttpMaidClient implements AutoCloseable {
    private final Issuer issuer;
    private final WebsocketClient websocketClient;
    private final BasePath basePath;
    private final FilterMap<Class<?>, ClientResponseMapper<?>> responseMappers;

    static HttpMaidClient httpMaidClient(final Issuer issuer,
                                         final BasePath basePath,
                                         final FilterMap<Class<?>, ClientResponseMapper<?>> responseMappers,
                                         final WebsocketClient websocketClient) {
        validateNotNull(issuer, "issuer");
        validateNotNull(basePath, "basePath");
        validateNotNull(responseMappers, "responseMappers");
        return new HttpMaidClient(issuer, websocketClient, basePath, responseMappers);
    }

    public static HttpMaidClientBuilder aHttpMaidClientBypassingRequestsDirectlyTo(final HttpMaid httpMaid) {
        validateNotNull(httpMaid, "httpMaid");
        final Issuer issuer = bypassIssuer(httpMaid);
        final BypassingWebsocketClient websocketClient = bypassingWebsocketClient(httpMaid);
        return clientBuilder(basePath -> issuer, websocketClient);
    }

    public static PortStage aHttpMaidClientForTheHost(final String host) {
        validateNotNullNorEmpty(host, "host");
        return port -> protocol -> {
            validateNotNull(protocol, "protocol");
            final WebsocketClient websocketClient = websocketClient(protocol, host, port);
            return clientBuilder(basePath -> realIssuer(protocol, host, port, basePath), websocketClient);
        };
    }

    public static PortStage aHttpMaidClientThatReusesConnectionsForTheHost(final String host) {
        validateNotNullNorEmpty(host, "host");
        return port -> protocol -> {
            validateNotNull(protocol, "protocol");
            final WebsocketClient websocketClient = websocketClient(protocol, host, port);
            return clientBuilder(basePath -> RealIssuer.realIssuerWithConnectionReuse(protocol, host, port, basePath), websocketClient);
        };
    }

    private static WebsocketClient websocketClient(final Protocol protocol,
                                                   final String host,
                                                   final int port) {
        final String websocketProtocol;
        if (protocol.equals(Protocol.HTTP)) {
            websocketProtocol = "ws";
        } else {
            websocketProtocol = "wss";
        }
        final String websocketUri = String.format("%s://%s:%d/", websocketProtocol, host, port);
        return realWebsocketClient(websocketUri);
    }

    public <T> T issue(final HttpClientRequestBuilder<T> requestBuilder) {
        return issue(requestBuilder.build(basePath));
    }

    @SuppressWarnings("unchecked")
    public <T> T issue(final HttpClientRequest<T> request) {
        validateNotNull(request, "request");
        return issuer.issue(request, response -> {
            final Class<T> targetType = request.targetType();
            final ClientResponseMapper<T> responseMapper = (ClientResponseMapper<T>) responseMappers.get(targetType);
            return responseMapper.map(response, targetType);
        });
    }

    public Websocket openWebsocket() {
        return openWebsocket(s -> {
        });
    }

    public Websocket openWebsocket(final WebsocketMessageHandler messageHandler) {
        return openWebsocket(messageHandler, Map.of(), Map.of());
    }

    public Websocket openWebsocket(final WebsocketMessageHandler messageHandler,
                                   final Map<String, String> queryParameters,
                                   final Map<String, List<String>> headers) {
        return websocketClient.openWebsocket(messageHandler, queryParameters, headers);
    }

    @Override
    public void close() {
        issuer.close();
    }
}
