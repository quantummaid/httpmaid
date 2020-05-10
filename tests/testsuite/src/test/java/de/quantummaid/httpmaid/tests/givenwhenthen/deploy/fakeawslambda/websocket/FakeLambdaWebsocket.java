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

package de.quantummaid.httpmaid.tests.givenwhenthen.deploy.fakeawslambda.websocket;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import de.quantummaid.httpmaid.awslambda.AwsLambdaEndpoint;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketListener;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class FakeLambdaWebsocket implements WebSocketListener {
    private final AwsLambdaEndpoint endpoint;
    private Session session;

    public static FakeLambdaWebsocket fakeLambdaWebsocket(final AwsLambdaEndpoint endpoint) {
        return new FakeLambdaWebsocket(endpoint);
    }

    @Override
    public synchronized void onWebSocketText(final String message) {
        final Map<String, Object> event = createEvent("MESSAGE");
        event.put("body", message);

        final APIGatewayProxyResponseEvent responseEvent = endpoint.delegate(event, null);
        final String body = responseEvent.getBody();
        if (body != null) {
            try {
                session.getRemote().sendString(body);
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void onWebSocketBinary(final byte[] bytes, final int i, final int i1) {

    }

    @Override
    public void onWebSocketClose(final int i, final String s) {

    }

    @Override
    public synchronized void onWebSocketConnect(final Session session) {
        this.session = session;
        final Map<String, Object> event = createEvent("CONNECT");
        final Map<String, String> queryParameters = new HashMap<>();
        session.getUpgradeRequest().getParameterMap()
                .forEach((key, values) -> queryParameters.put(key, values.get(0)));
        event.put("queryStringParameters", queryParameters);
        final Map<String, List<String>> headers = session.getUpgradeRequest().getHeaders();
        event.put("multiValueHeaders", headers);
        endpoint.delegate(event, null);
    }

    @Override
    public void onWebSocketError(final Throwable throwable) {

    }

    private static Map<String, Object> createEvent(final String eventType) {
        final Map<String, Object> requestContext = new HashMap<>();
        requestContext.put("eventType", eventType);
        requestContext.put("connectionId", "fake-id");
        requestContext.put("stage", "fake");
        requestContext.put("apiId", "fake");
        requestContext.put("domainName", "fake.execute-api.fake-1.amazonaws.com");
        final Map<String, Object> event = new HashMap<>();
        event.put("requestContext", requestContext);
        return event;
    }
}
