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

package de.quantummaid.httpmaid.tests.deployers.fakeawslambda;

import de.quantummaid.httpmaid.tests.deployers.fakeawslambda.websocket.ApiWebsockets;
import de.quantummaid.httpmaid.util.streams.Streams;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

import static de.quantummaid.httpmaid.util.streams.Streams.inputStreamToString;
import static de.quantummaid.httpmaid.util.streams.Streams.streamInputStreamToOutputStream;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class FakeApiGatewayManagementServer implements AutoCloseable {
    private static final int OK = 200;
    private final Server server;

    public static FakeApiGatewayManagementServer start(final int port,
                                                       final ApiWebsockets apiWebsockets) {
        final Server server = Server.server(port, exchange -> {
            final URI requestURI = exchange.getRequestURI();
            final String path = requestURI.getPath();
            final String connectionId = path.split("/")[2];
            final String requestMethod = exchange.getRequestMethod();
            if ("DELETE".equals(requestMethod)) {
                delete(connectionId, apiWebsockets);
                exchange.sendResponseHeaders(OK, 0);
                exchange.close();
            } else if ("POST".equals(requestMethod)) {
                final InputStream bodyStream = exchange.getRequestBody();
                final String bodyString = inputStreamToString(bodyStream);
                post(connectionId, bodyString, apiWebsockets);
                exchange.sendResponseHeaders(OK, 0);
                final OutputStream outputStream = exchange.getResponseBody();
                final InputStream responseStream = Streams.stringToInputStream("{}");
                streamInputStreamToOutputStream(responseStream, outputStream);
            } else {
                throw new UnsupportedOperationException("Unsupported method: " + requestMethod);
            }
        });
        return new FakeApiGatewayManagementServer(server);
    }

    private static void post(final String connectionId,
                             final String message,
                             final ApiWebsockets apiWebsockets) {
        apiWebsockets.send(connectionId, message);
    }

    private static void delete(final String connectionId,
                               final ApiWebsockets apiWebsockets) {
        apiWebsockets.disconnect(connectionId);
    }

    @Override
    public void close() {
        server.close();
    }
}
