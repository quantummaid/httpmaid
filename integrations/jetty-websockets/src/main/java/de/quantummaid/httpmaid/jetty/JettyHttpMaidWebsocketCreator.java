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
import de.quantummaid.httpmaid.http.Headers;
import de.quantummaid.httpmaid.websockets.authorization.AuthorizationDecision;
import de.quantummaid.httpmaid.websockets.endpoint.RawWebsocketAuthorizationBuilder;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.eclipse.jetty.websocket.servlet.WebSocketCreator;

import java.util.List;
import java.util.Map;

import static de.quantummaid.httpmaid.jetty.JettyEndpointException.jettyEndpointException;
import static de.quantummaid.httpmaid.jetty.JettyHttpMaidWebsocket.jettyHttpMaidWebsocket;
import static de.quantummaid.httpmaid.jetty.UpgradeRequestUtils.extractHeaders;
import static de.quantummaid.httpmaid.jetty.UpgradeRequestUtils.extractQueryParameters;
import static de.quantummaid.httpmaid.websockets.authorization.AuthorizationDecision.AUTHORIZATION_DECISION;
import static de.quantummaid.httpmaid.websockets.endpoint.RawWebsocketAuthorizationBuilder.rawWebsocketAuthorizationBuilder;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class JettyHttpMaidWebsocketCreator implements WebSocketCreator {
    private final HttpMaid httpMaid;

    public static JettyHttpMaidWebsocketCreator fakeLambdaWebsocketCreator(final HttpMaid httpMaid) {
        return new JettyHttpMaidWebsocketCreator(httpMaid);
    }

    @Override
    public Object createWebSocket(final ServletUpgradeRequest request,
                                  final ServletUpgradeResponse response) {
        final AuthorizationDecision authorizationDecision = httpMaid.handleRequestSynchronously(() -> {
            final RawWebsocketAuthorizationBuilder builder = rawWebsocketAuthorizationBuilder();

            final Map<String, List<String>> queryParameters = extractQueryParameters(request);
            builder.withQueryParameterMap(queryParameters);
            final Headers headers = extractHeaders(request);
            builder.withHeaders(headers);

            return builder.build();
        }, authorizationResponse -> authorizationResponse.metaData().get(AUTHORIZATION_DECISION));

        if (!authorizationDecision.isAuthorized()) {
            throw jettyEndpointException("not authorized");
        }

        final Map<String, Object> additionalData = authorizationDecision.additionalData();
        return jettyHttpMaidWebsocket(httpMaid, additionalData);
    }
}
