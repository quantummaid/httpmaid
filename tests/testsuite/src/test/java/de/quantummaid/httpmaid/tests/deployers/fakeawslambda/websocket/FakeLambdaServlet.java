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

import de.quantummaid.httpmaid.awslambda.AwsWebsocketLambdaEndpoint;
import de.quantummaid.httpmaid.awslambda.authorizer.LambdaWebsocketAuthorizer;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

import static de.quantummaid.httpmaid.tests.deployers.fakeawslambda.websocket.FakeLambdaWebsocketCreator.fakeLambdaWebsocketCreator;

@ToString
@EqualsAndHashCode(callSuper = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class FakeLambdaServlet extends WebSocketServlet {
    private static final long serialVersionUID = 1L;
    private final transient AwsWebsocketLambdaEndpoint endpoint;
    private final transient LambdaWebsocketAuthorizer authorizer;
    private final transient ApiWebsockets apiWebsockets;

    public static FakeLambdaServlet fakeLambdaServlet(final AwsWebsocketLambdaEndpoint endpoint,
                                                      final LambdaWebsocketAuthorizer authorizer,
                                                      final ApiWebsockets apiWebsockets) {
        return new FakeLambdaServlet(endpoint, authorizer, apiWebsockets);
    }

    @Override
    public void configure(final WebSocketServletFactory webSocketServletFactory) {
        webSocketServletFactory.setCreator(fakeLambdaWebsocketCreator(endpoint, authorizer, apiWebsockets));
    }
}
