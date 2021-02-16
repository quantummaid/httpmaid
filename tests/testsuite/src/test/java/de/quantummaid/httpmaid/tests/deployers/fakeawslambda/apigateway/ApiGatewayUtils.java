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
import de.quantummaid.httpmaid.util.streams.Streams;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLDecoder;
import java.util.*;

import static de.quantummaid.httpmaid.lambdastructure.Structures.HTTP_REQUEST_V1;
import static de.quantummaid.httpmaid.lambdastructure.Structures.REST_REQUEST;
import static de.quantummaid.httpmaid.util.streams.Streams.inputStreamToString;
import static de.quantummaid.httpmaid.util.streams.Streams.streamInputStreamToOutputStream;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.emptyMap;
import static java.util.Optional.ofNullable;

public final class ApiGatewayUtils {

    private ApiGatewayUtils() {
    }

    public static void addBodyToEvent(final InputStream bodyStream, final Map<String, Object> event) {
        final String body = inputStreamToString(bodyStream);
        addBodyToEvent(body, event);
    }

    public static void addBodyToEvent(final String body, final Map<String, Object> event) {
        if (body.isEmpty()) {
            event.put("body", null);
            event.put("isBase64Encoded", false);
        } else {
            final String encodedBody = encodeBase64(body);
            event.put("body", encodedBody);
            event.put("isBase64Encoded", true);
        }
    }

    private static String encodeBase64(final String unencoded) {
        final Base64.Encoder encoder = Base64.getEncoder();
        final byte[] bytes = encoder.encode(unencoded.getBytes(UTF_8));
        return new String(bytes, UTF_8);
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> mapRequestToHttpApiV1PayloadEvent(final HttpExchange exchange) {
        final Map<String, Object> event = (Map<String, Object>) HTTP_REQUEST_V1.mutableSample();
        mapRequestToEvent(exchange, event);
        return event;
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> mapRequestToRestApiEvent(final HttpExchange exchange) {
        final Map<String, Object> event = (Map<String, Object>) REST_REQUEST.mutableSample();
        mapRequestToEvent(exchange, event);
        return event;
    }

    private static void mapRequestToEvent(final HttpExchange exchange,
                                          final Map<String, Object> event) {
        final URI requestURI = exchange.getRequestURI();
        final String path = requestURI.getPath();
        event.put("path", path);
        final Map<String, List<String>> queryParameters = queryToMap(requestURI.getRawQuery());
        event.put("multiValueQueryStringParameters", queryParameters);
        final String method = exchange.getRequestMethod();
        event.put("httpMethod", method);
        addBodyToEvent(exchange.getRequestBody(), event);
        final Map<String, List<String>> headers = new HashMap<>();
        exchange.getRequestHeaders().forEach(headers::put);
        event.put("multiValueHeaders", headers);
    }

    public static void mapRestApiEventToResponse(final Map<String, Object> event,
                                                 final HttpExchange exchange) throws IOException {
        final Headers responseHeaders = exchange.getResponseHeaders();
        @SuppressWarnings("unchecked") final Map<String, List<String>> headerMap = (Map<String, List<String>>) ofNullable(event.get("multiValueHeaders")).orElse(emptyMap());
        responseHeaders.putAll(headerMap);
        final Integer statusCode = (Integer) event.get("statusCode");
        exchange.sendResponseHeaders(statusCode, 0);
        final InputStream bodyStream = Streams.stringToInputStream((String) event.get("body"));
        streamInputStreamToOutputStream(bodyStream, exchange.getResponseBody());
    }

    private static Map<String, List<String>> queryToMap(final String query) {
        final Map<String, List<String>> result = new HashMap<>();
        if (query == null) {
            return result;
        }
        for (final String param : query.split("&")) {
            final String[] entry = param.split("=");
            final String key = decode(entry[0]);
            final String value;
            if (entry.length > 1) {
                value = decode(entry[1]);
            } else {
                value = "";
            }
            final List<String> values = result.getOrDefault(key, new ArrayList<>());
            values.add(value);
            result.put(key, values);
        }
        return result;
    }

    private static String decode(final String s) {
        return URLDecoder.decode(s, UTF_8);
    }
}
