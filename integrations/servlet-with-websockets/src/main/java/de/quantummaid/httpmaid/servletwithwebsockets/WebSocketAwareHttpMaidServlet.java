/*
 * Copyright (c) 2019 Richard Hauswald - https://quantummaid.de/.
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

package de.quantummaid.httpmaid.servletwithwebsockets;

import de.quantummaid.httpmaid.HttpMaid;
import de.quantummaid.httpmaid.chains.MetaData;
import de.quantummaid.httpmaid.websockets.registry.WebSocketId;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static de.quantummaid.httpmaid.servlet.ServletHandling.extractMetaDataFromHttpServletRequest;
import static de.quantummaid.httpmaid.servlet.ServletHandling.handle;
import static de.quantummaid.httpmaid.websockets.WebsocketChainKeys.*;
import static de.quantummaid.httpmaid.websockets.registry.WebSocketId.randomWebSocketId;

@ToString
@EqualsAndHashCode(callSuper = false)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class WebSocketAwareHttpMaidServlet extends WebSocketServlet {
    private static final long serialVersionUID = 1;

    private final transient HttpMaid httpMaid;

    public static WebSocketAwareHttpMaidServlet webSocketAwareHttpMaidServlet(final HttpMaid httpMaid) {
        return new WebSocketAwareHttpMaidServlet(httpMaid);
    }

    @Override
    public void configure(final WebSocketServletFactory webSocketServletFactory) {
        webSocketServletFactory.setCreator((servletUpgradeRequest, servletUpgradeResponse) -> {
            final MetaData metaData = extractMetaDataFromHttpServletRequest(servletUpgradeRequest.getHttpServletRequest());
            final WebSocketId webSocketId = randomWebSocketId();
            metaData.set(WEBSOCKET_ID, webSocketId);
            final JettyStyleWebSocket jettyStyleSocket = JettyStyleWebSocket.jettyStyleSocket(httpMaid, webSocketId);
            metaData.set(WEBSOCKET_DELEGATE, jettyStyleSocket);

            httpMaid.handleRequest(metaData, m -> {
            });

            if (metaData.getOptional(WEBSOCKET_ACCEPTED).orElse(false)) {
                return jettyStyleSocket;
            } else {
                return null;
            }
        });
    }

    @Override
    protected void doGet(final HttpServletRequest request,
                         final HttpServletResponse response) throws IOException {
        handle(httpMaid, request, response);
    }

    @Override
    protected void doPost(final HttpServletRequest request,
                          final HttpServletResponse response) throws IOException {
        handle(httpMaid, request, response);
    }

    @Override
    protected void doPut(final HttpServletRequest request,
                         final HttpServletResponse response) throws IOException {
        handle(httpMaid, request, response);
    }

    @Override
    protected void doDelete(final HttpServletRequest request,
                            final HttpServletResponse response) throws IOException {
        handle(httpMaid, request, response);
    }

    @Override
    protected void doOptions(final HttpServletRequest request,
                             final HttpServletResponse response) throws IOException {
        handle(httpMaid, request, response);
    }
}
