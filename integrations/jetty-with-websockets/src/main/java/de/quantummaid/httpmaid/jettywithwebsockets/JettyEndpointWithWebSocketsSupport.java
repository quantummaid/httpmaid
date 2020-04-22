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

package de.quantummaid.httpmaid.jettywithwebsockets;

import de.quantummaid.httpmaid.HttpMaid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import javax.servlet.Servlet;

import static de.quantummaid.httpmaid.closing.ClosingActions.CLOSING_ACTIONS;
import static de.quantummaid.httpmaid.jettywithwebsockets.JettyEndpointException.jettyEndpointException;
import static de.quantummaid.httpmaid.servletwithwebsockets.WebSocketAwareHttpMaidServlet.webSocketAwareHttpMaidServlet;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class JettyEndpointWithWebSocketsSupport implements AutoCloseable {
    private final HttpMaid httpMaid;

    public static PortStage jettyEndpointWithWebSocketsSupportFor(final HttpMaid httpMaid) {
        return port -> {
            try {
                final Server server = new Server(port);
                final ServletHandler servletHandler = new ServletHandler();
                server.setHandler(servletHandler);
                final Servlet servlet = webSocketAwareHttpMaidServlet(httpMaid);
                final ServletHolder servletHolder = new ServletHolder(servlet);
                servletHandler.addServletWithMapping(servletHolder, "/*");
                server.start();
                httpMaid.getMetaDatum(CLOSING_ACTIONS).addClosingAction(() -> {
                    try {
                        server.stop();
                    } catch (final Exception e) {
                        throw new RuntimeException(e);
                    }
                });
            } catch (final Exception e) {
                throw jettyEndpointException(e);
            }
            return new JettyEndpointWithWebSocketsSupport(httpMaid);
        };
    }

    @Override
    public void close() {
        httpMaid.close();
    }
}
