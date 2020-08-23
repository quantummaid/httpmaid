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
import de.quantummaid.httpmaid.closing.ClosingAction;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.eclipse.jetty.server.ConnectionFactory;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;

import java.util.function.Consumer;

import static de.quantummaid.httpmaid.closing.ClosingActions.CLOSING_ACTIONS;
import static de.quantummaid.httpmaid.jetty.JettyEndpointException.jettyEndpointException;
import static de.quantummaid.httpmaid.jetty.JettyEndpointHandler.jettyEndpointHandler;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class JettyEndpoint implements AutoCloseable {
    private final HttpMaid httpMaid;

    public static PortStage jettyEndpointFor(final HttpMaid httpMaid) {
        return port -> jettyEndpoint(port,
                httpMaid,
                server -> server.setHandler(jettyEndpointHandler(httpMaid)));
    }

    static JettyEndpoint jettyEndpoint(final int port,
                                       final HttpMaid httpMaid,
                                       final Consumer<Server> configuration) {
        try {
            final Server server = new Server(port);
            final HttpConnectionFactory connectionFactory = extractConnectionFactory(server);
            connectionFactory.getHttpConfiguration().setFormEncodedMethods();
            configuration.accept(server);
            server.start();
            httpMaid.getMetaDatum(CLOSING_ACTIONS).addClosingAction(closeJetty(server));
            return new JettyEndpoint(httpMaid);
        } catch (final Exception e) {
            throw jettyEndpointException("Could not create Jetty Endpoint", e);
        }
    }

    private static ClosingAction closeJetty(final Server server) {
        return () -> {
            try {
                server.stop();
                server.destroy();
            } catch (final Exception e) {
                throw jettyEndpointException("Could not stop Jetty Endpoint", e);
            }
        };
    }

    @Override
    public void close() {
        httpMaid.close();
    }

    private static HttpConnectionFactory extractConnectionFactory(final Server server) {
        final Connector[] connectors = server.getConnectors();
        if (connectors.length != 1) {
            throw new UnsupportedOperationException("Jetty does not behave as expected");
        }
        final Connector connector = connectors[0];
        final ConnectionFactory connectionFactory = connector.getDefaultConnectionFactory();
        if (!(connectionFactory instanceof HttpConnectionFactory)) {
            throw new UnsupportedOperationException("Jetty does not behave as expected");
        }
        return (HttpConnectionFactory) connectionFactory;
    }
}
