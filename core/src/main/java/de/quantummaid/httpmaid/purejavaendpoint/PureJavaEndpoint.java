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

package de.quantummaid.httpmaid.purejavaendpoint;

import de.quantummaid.httpmaid.HttpMaid;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.net.InetSocketAddress;

import static de.quantummaid.httpmaid.purejavaendpoint.PureJavaEndpointHandler.javaOnlyEndpointHandler;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class PureJavaEndpoint implements AutoCloseable {
    private final HttpServer httpServer;

    public static PortStage pureJavaEndpointFor(final HttpMaid httpMaid) {
        return port -> {
            final HttpServer httpServer;
            try {
                httpServer = HttpServer.create(new InetSocketAddress(port), 0);
                final HttpHandler httpHandler = javaOnlyEndpointHandler(httpMaid);
                httpServer.createContext("/", httpHandler);
                httpServer.setExecutor(null);
                httpServer.start();
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
            return new PureJavaEndpoint(httpServer);
        };
    }

    @Override
    public void close() {
        httpServer.stop(0);
    }
}