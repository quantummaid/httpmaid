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

package de.quantummaid.httpmaid.undertow;

import de.quantummaid.httpmaid.HttpMaid;
import de.quantummaid.httpmaid.endpoint.RawHttpRequest;
import de.quantummaid.httpmaid.endpoint.RawHttpRequestBuilder;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HeaderMap;
import io.undertow.util.HttpString;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class UndertowHandler implements HttpHandler {
    private final HttpMaid httpMaid;

    public static UndertowHandler undertowHandler(final HttpMaid httpMaid) {
        return new UndertowHandler(httpMaid);
    }

    @Override
    public void handleRequest(final HttpServerExchange httpServerExchange) {
        if (httpServerExchange.isInIoThread()) {
            httpServerExchange.dispatch(this);
            return;
        }
        httpMaid.handleRequest(() -> {
            final RawHttpRequestBuilder builder = RawHttpRequest.rawHttpRequestBuilder();

            final Map<String, List<String>> headers = extractHeaders(httpServerExchange);
            builder.withHeaders(headers);

            final String path = httpServerExchange.getRequestPath();
            builder.withPath(path);

            final HttpString requestMethod = httpServerExchange.getRequestMethod();
            builder.withMethod(requestMethod.toString());

            final Map<String, Deque<String>> queryParameters = httpServerExchange.getQueryParameters();
            builder.withQueryParameters(queryParameters);

            httpServerExchange.startBlocking();
            final InputStream body = httpServerExchange.getInputStream();
            builder.withBody(body);

            return builder.build();
        }, response -> {
            final int status = response.status();
            httpServerExchange.setStatusCode(status);

            final HeaderMap responseHeaders = httpServerExchange.getResponseHeaders();
            response.headers().forEach((name, values) -> {
                responseHeaders.putAll(HttpString.tryFromString(name), values);
            });

            final OutputStream outputStream = httpServerExchange.getOutputStream();
            response.streamBodyToOutputStream(outputStream);
        });
    }

    private static Map<String, List<String>> extractHeaders(final HttpServerExchange httpServerExchange) {
        final Map<String, List<String>> headerMap = new HashMap<>();
        final HeaderMap requestHeaders = httpServerExchange.getRequestHeaders();
        requestHeaders.getHeaderNames().forEach(httpString -> {
            final List<String> headerValues = requestHeaders.get(httpString);
            headerMap.put(httpString.toString(), headerValues);
        });
        return headerMap;
    }
}
