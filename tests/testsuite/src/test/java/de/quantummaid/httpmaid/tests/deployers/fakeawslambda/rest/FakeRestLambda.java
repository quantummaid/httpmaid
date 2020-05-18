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

package de.quantummaid.httpmaid.tests.deployers.fakeawslambda.rest;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import de.quantummaid.httpmaid.awslambda.AwsLambdaEndpoint;
import de.quantummaid.httpmaid.util.streams.Streams;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.quantummaid.httpmaid.tests.deployers.fakeawslambda.FakeAwsContext.fakeAwsContext;
import static de.quantummaid.httpmaid.util.streams.Streams.inputStreamToString;
import static de.quantummaid.httpmaid.util.streams.Streams.streamInputStreamToOutputStream;
import static java.util.Collections.singletonList;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class FakeRestLambda implements AutoCloseable {
    private final HttpServer server;

    public static FakeRestLambda fakeRestLambda(final AwsLambdaEndpoint endpoint,
                                                final int port) {
        try {
            final HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
            final HttpHandler httpHandler = exchange -> {
                final Map<String, Object> requestEvent = mapRequestToEvent(exchange);
                final APIGatewayProxyResponseEvent responseEvent = endpoint.delegate(requestEvent, fakeAwsContext());
                mapEventToResponse(responseEvent, exchange);
            };
            server.createContext("/", httpHandler);
            server.setExecutor(null);
            server.start();
            return new FakeRestLambda(server);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        server.stop(0);
    }

    private static Map<String, Object> mapRequestToEvent(final HttpExchange exchange) {
        final Map<String, Object> event = new HashMap<>();
        final URI requestURI = exchange.getRequestURI();
        final String path = requestURI.getPath();
        event.put("path", path);
        final Map<String, String> queryParameters = queryToMap(requestURI.getQuery());
        event.put("queryStringParameters", queryParameters);
        final String method = exchange.getRequestMethod();
        event.put("httpMethod", method);
        final String body = inputStreamToString(exchange.getRequestBody());
        event.put("body", body);
        final Map<String, List<String>> headers = new HashMap<>();
        exchange.getRequestHeaders().forEach(headers::put);
        event.put("multiValueHeaders", headers);
        return event;
    }

    private static void mapEventToResponse(final APIGatewayProxyResponseEvent event,
                                           final HttpExchange exchange) throws IOException {
        final Headers responseHeaders = exchange.getResponseHeaders();
        event.getHeaders().forEach((key, value) -> responseHeaders.put(key, singletonList(value)));
        final Integer statusCode = event.getStatusCode();
        exchange.sendResponseHeaders(statusCode, 0);
        final InputStream bodyStream = Streams.stringToInputStream(event.getBody());
        streamInputStreamToOutputStream(bodyStream, exchange.getResponseBody());
    }

    private static Map<String, String> queryToMap(final String query) {
        final Map<String, String> result = new HashMap<>();
        if (query == null) {
            return result;
        }
        for (final String param : query.split("&")) {
            final String[] entry = param.split("=");
            if (entry.length > 1) {
                result.put(entry[0], entry[1]);
            } else {
                result.put(entry[0], "");
            }
        }
        return result;
    }
}
