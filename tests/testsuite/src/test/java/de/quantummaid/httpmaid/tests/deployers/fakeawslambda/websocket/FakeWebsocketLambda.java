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
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.eclipse.jetty.server.ConnectionFactory;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import static de.quantummaid.httpmaid.tests.deployers.fakeawslambda.websocket.FakeLambdaServlet.fakeLambdaServlet;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class FakeWebsocketLambda implements AutoCloseable {
    private final Server server;

    public static FakeWebsocketLambda fakeWebsocketLambda(final AwsWebsocketLambdaEndpoint endpoint,
                                                          final int port,
                                                          final ApiWebsockets apiWebsockets) {
        final Server server = new Server(port);

        final HttpConnectionFactory connectionFactory = extractConnectionFactory(server);
        connectionFactory.getHttpConfiguration().setFormEncodedMethods();

        final ServletHandler servletHandler = new ServletHandler();
        server.setHandler(servletHandler);
        final ServletHolder servletHolder = new ServletHolder(fakeLambdaServlet(endpoint, apiWebsockets));
        servletHandler.addServletWithMapping(servletHolder, "/*");
        try {
            server.start();
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
        return new FakeWebsocketLambda(server);
    }

    @Override
    public void close() {
        try {
            server.stop();
            server.destroy();
        } catch (Exception e) {
            throw new UnsupportedOperationException("Could not stop jetty", e);
        }
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
