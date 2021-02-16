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
import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.eclipse.jetty.websocket.servlet.WebSocketCreator;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static de.quantummaid.httpmaid.lambdastructure.Structures.WEBSOCKET_AUTHORIZATION;
import static de.quantummaid.httpmaid.tests.deployers.fakeawslambda.websocket.EventUtils.addFullWebsocketMetaData;
import static de.quantummaid.httpmaid.tests.deployers.fakeawslambda.websocket.FakeLambdaWebsocket.fakeLambdaWebsocket;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class FakeLambdaWebsocketCreator implements WebSocketCreator {
    private final ValidatedAwsLambdaEndpoint endpoint;
    private final ApiWebsockets apiWebsockets;

    public static FakeLambdaWebsocketCreator fakeLambdaWebsocketCreator(final ValidatedAwsLambdaEndpoint endpoint,
                                                                        final ApiWebsockets apiWebsockets) {
        return new FakeLambdaWebsocketCreator(endpoint, apiWebsockets);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object createWebSocket(final ServletUpgradeRequest request,
                                  final ServletUpgradeResponse response) {
        final Map<String, Object> authorizationContext;
        if (endpoint.hasAuthorizer()) {
            final Map<String, Object> authorizationEvent = (Map<String, Object>) WEBSOCKET_AUTHORIZATION.mutableSample();
            addFullWebsocketMetaData(request, authorizationEvent);
            final Map<String, Object> authorizationResponse = endpoint.delegate(authorizationEvent);
            final Map<String, Object> policyDocument = (Map<String, Object>) authorizationResponse.get("policyDocument");
            final List<Map<String, Object>> statements = (List<Map<String, Object>>) policyDocument.get("Statement");
            final Map<String, Object> statement = statements.get(0);
            if (!"Allow".equals(statement.get("Effect"))) {
                throw new RuntimeException("unauthorized: " + authorizationResponse);
            }
            authorizationContext = (Map<String, Object>) authorizationResponse.get("context");
        } else {
            authorizationContext = null;
        }

        final String connectionId = UUID.randomUUID().toString();
        final FakeLambdaWebsocket websocket = fakeLambdaWebsocket(endpoint, connectionId, authorizationContext);
        apiWebsockets.add(connectionId, websocket);
        return websocket;
    }
}
