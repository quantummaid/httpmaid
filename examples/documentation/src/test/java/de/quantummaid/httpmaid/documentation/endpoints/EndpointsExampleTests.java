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

package de.quantummaid.httpmaid.documentation.endpoints;

import de.quantummaid.httpmaid.HttpMaid;
import de.quantummaid.httpmaid.client.HttpClientRequest;
import de.quantummaid.httpmaid.client.HttpMaidClient;
import de.quantummaid.httpmaid.endpoint.purejavaendpoint.PureJavaEndpoint;
import de.quantummaid.httpmaid.jetty.JettyEndpoint;
import de.quantummaid.httpmaid.jetty.JettyWebsocketEndpoint;
import de.quantummaid.httpmaid.servlet.ServletEndpoint;
import org.eclipse.jetty.server.ConnectionFactory;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.junit.jupiter.api.Test;

import javax.servlet.http.HttpServlet;

import static de.quantummaid.httpmaid.HttpMaid.anHttpMaid;
import static de.quantummaid.httpmaid.documentation.support.FreePortPool.freePort;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public final class EndpointsExampleTests {

    @Test
    public void pureJavaEndpoint() {
        final HttpMaid httpMaid = httpMaid();
        final int port = freePort();
        //Showcase start javaEndpoint
        PureJavaEndpoint.pureJavaEndpointFor(httpMaid).listeningOnThePort(port);
        //Showcase end javaEndpoint
        assertForPort(port);
        httpMaid.close();
    }

    @Test
    public void jettyEndpoint() {
        final HttpMaid httpMaid = httpMaid();
        final int port = freePort();
        //Showcase start jettyEndpoint
        JettyEndpoint.jettyEndpointFor(httpMaid).listeningOnThePort(port);
        //Showcase end jettyEndpoint
        assertForPort(port);
        httpMaid.close();
    }

    @Test
    public void undertowEndpoint() {
        final HttpMaid httpMaid = httpMaid();
        final int port = freePort();
        //Showcase start jettyWebsocketEndpoint
        JettyWebsocketEndpoint.jettyWebsocketEndpoint(httpMaid, port);
        //Showcase end jettyWebsocketEndpoint
        assertForPort(port);
        httpMaid.close();
    }

    @Test
    public void servletEndpoint() {
        final HttpMaid httpMaid = httpMaid();

        //Showcase start servletSample
        final HttpServlet servlet = ServletEndpoint.servletEndpointFor(httpMaid);
        //Showcase end servletSample

        final int port = freePort();
        final Server server = servletDeploy(servlet, port);
        assertForPort(port);

        try {
            server.stop();
            server.destroy();
        } catch (Exception e) {
            throw new UnsupportedOperationException("Could not stop jetty", e);
        }
    }

    private static HttpMaid httpMaid() {
        return anHttpMaid()
                .get("/foo", (request, response) -> response.setBody("foo"))
                .build();
    }

    private static void assertForPort(final int port) {
        final HttpMaidClient client = HttpMaidClient.aHttpMaidClientForTheHost("localhost")
                .withThePort(port)
                .viaHttp()
                .build();
        final String response = client.issue(HttpClientRequest.aGetRequestToThePath("/foo").mappedToString());
        assertThat(response, is("foo"));
    }

    private static Server servletDeploy(final HttpServlet servlet, final int port) {
        final Server server = new Server(port);

        final HttpConnectionFactory connectionFactory = extractConnectionFactory(server);
        connectionFactory.getHttpConfiguration().setFormEncodedMethods();

        final ServletHandler servletHandler = new ServletHandler();
        server.setHandler(servletHandler);
        final ServletHolder servletHolder = new ServletHolder(servlet);
        servletHandler.addServletWithMapping(servletHolder, "/*");
        try {
            server.start();
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
        return server;
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
