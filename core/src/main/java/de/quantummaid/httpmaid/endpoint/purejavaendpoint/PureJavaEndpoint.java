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

package de.quantummaid.httpmaid.endpoint.purejavaendpoint;

import com.sun.net.httpserver.HttpHandler; // NOSONAR
import com.sun.net.httpserver.HttpServer; // NOSONAR
import de.quantummaid.httpmaid.HttpMaid;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.io.IOException;
import java.net.InetSocketAddress;

import static de.quantummaid.httpmaid.closing.ClosingActions.CLOSING_ACTIONS;
import static de.quantummaid.httpmaid.endpoint.purejavaendpoint.PureJavaEndpointException.pureJavaEndpointException;
import static de.quantummaid.httpmaid.endpoint.purejavaendpoint.PureJavaEndpointHandler.javaOnlyEndpointHandler;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class PureJavaEndpoint implements AutoCloseable {
    private final HttpMaid httpMaid;

    public static PortStage pureJavaEndpointFor(final HttpMaid httpMaid) {
        return port -> {
            try {
                final HttpServer httpServer = HttpServer.create(new InetSocketAddress(port), 0);
                final HttpHandler httpHandler = javaOnlyEndpointHandler(httpMaid);
                httpServer.createContext("/", httpHandler);
                httpServer.setExecutor(null);
                httpServer.start();
                httpMaid.getMetaDatum(CLOSING_ACTIONS).addClosingAction(() -> httpServer.stop(0));
            } catch (final IOException e) {
                throw pureJavaEndpointException(e);
            }
            return new PureJavaEndpoint(httpMaid);
        };
    }

    @Override
    public void close() {
        httpMaid.close();
    }
}
