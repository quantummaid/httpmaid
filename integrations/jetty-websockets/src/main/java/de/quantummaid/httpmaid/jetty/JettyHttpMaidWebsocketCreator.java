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
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.eclipse.jetty.websocket.servlet.WebSocketCreator;

import static de.quantummaid.httpmaid.jetty.JettyHttpMaidWebsocket.jettyHttpMaidWebsocket;

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
        return jettyHttpMaidWebsocket(httpMaid);
    }
}
