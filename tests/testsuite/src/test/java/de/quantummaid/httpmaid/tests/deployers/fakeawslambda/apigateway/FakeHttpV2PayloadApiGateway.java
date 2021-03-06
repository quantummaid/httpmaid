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

package de.quantummaid.httpmaid.tests.deployers.fakeawslambda.apigateway;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import de.quantummaid.httpmaid.tests.deployers.fakeawslambda.Server;
import de.quantummaid.httpmaid.tests.deployers.fakeawslambda.ValidatedAwsLambdaEndpoint;
import de.quantummaid.httpmaid.util.streams.Streams;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.*;

import static de.quantummaid.httpmaid.lambdastructure.Structures.HTTP_REQUEST_V2;
import static de.quantummaid.httpmaid.tests.deployers.fakeawslambda.apigateway.ApiGatewayUtils.addBodyToEvent;
import static de.quantummaid.httpmaid.util.streams.Streams.inputStreamToString;
import static de.quantummaid.httpmaid.util.streams.Streams.streamInputStreamToOutputStream;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class FakeHttpV2PayloadApiGateway implements AutoCloseable {
    private final Server server;

    public static FakeHttpV2PayloadApiGateway fakeHttpV2PayloadApiGateway(final ValidatedAwsLambdaEndpoint endpoint,
                                                                          final int port) {
        final Server server = Server.server(port, exchange -> {
            try {
                final Map<String, Object> requestEvent = mapRequestToEvent(exchange);
                final Map<String, Object> responseEvent = endpoint.delegate(requestEvent);
                mapEventToResponse(responseEvent, exchange);
            } catch (final Exception e) {
                e.printStackTrace(System.err);
                throw e;
            }
        });
        return new FakeHttpV2PayloadApiGateway(server);
    }

    @Override
    public void close() {
        server.close();
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> mapRequestToEvent(final HttpExchange exchange) {
        final Map<String, Object> event = (Map<String, Object>) HTTP_REQUEST_V2.mutableSample();
        event.put("version", "2.0");

        final Map<String, Object> requestContext = (Map<String, Object>) event.get("requestContext");
        final Map<String, Object> httpInformation = (Map<String, Object>) requestContext.get("http");

        final URI requestURI = exchange.getRequestURI();
        final String path = requestURI.getPath();
        httpInformation.put("path", path);

        final String method = exchange.getRequestMethod();
        httpInformation.put("method", method);

        final Map<String, String> headers = new LinkedHashMap<>();
        final List<String> cookies = new ArrayList<>();
        exchange.getRequestHeaders().forEach((name, values) -> {
            if (name.equalsIgnoreCase("Cookie")) {
                cookies.addAll(values);
            } else {
                final String joinedHeaders = String.join(",", values);
                headers.put(name, joinedHeaders);
            }
        });
        event.put("headers", headers);
        event.put("cookies", cookies);

        final String rawQuery = Optional.ofNullable(requestURI.getRawQuery()).orElse("");
        event.put("rawQueryString", rawQuery);

        final String body = inputStreamToString(exchange.getRequestBody());
        if (!body.isEmpty()) {
            addBodyToEvent(body, event);
        }
        return event;
    }

    @SuppressWarnings("unchecked")
    private static void mapEventToResponse(final Map<String, Object> event,
                                           final HttpExchange exchange) throws IOException {
        final Headers responseHeaders = exchange.getResponseHeaders();
        final Map<String, String> headers = (Map<String, String>) event.get("headers");
        headers.forEach(responseHeaders::add);
        final List<String> cookies = (List<String>) event.get("cookies");
        if (!cookies.isEmpty()) {
            responseHeaders.put("Set-Cookie", cookies);
        }
        final Integer statusCode = (Integer) event.get("statusCode");
        exchange.sendResponseHeaders(statusCode, 0);
        final InputStream bodyStream = Streams.stringToInputStream((String) event.get("body"));
        streamInputStreamToOutputStream(bodyStream, exchange.getResponseBody());
    }
}
