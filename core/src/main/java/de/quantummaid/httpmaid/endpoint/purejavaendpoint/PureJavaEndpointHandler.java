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

import com.sun.net.httpserver.Headers; // NOSONAR
import com.sun.net.httpserver.HttpExchange; // NOSONAR
import com.sun.net.httpserver.HttpHandler; // NOSONAR
import de.quantummaid.httpmaid.HttpMaid;
import de.quantummaid.httpmaid.endpoint.RawHttpRequestBuilder;
import de.quantummaid.httpmaid.http.HeadersBuilder;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

import static de.quantummaid.httpmaid.endpoint.RawHttpRequest.rawHttpRequestBuilder;
import static de.quantummaid.httpmaid.http.HeadersBuilder.headersBuilder;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
final class PureJavaEndpointHandler implements HttpHandler {
    private final HttpMaid httpMaid;

    static HttpHandler javaOnlyEndpointHandler(final HttpMaid httpMaid) {
        return new PureJavaEndpointHandler(httpMaid);
    }

    @Override
    public void handle(final HttpExchange httpExchange) {
        httpMaid.handleRequest(() -> {
                    final RawHttpRequestBuilder builder = rawHttpRequestBuilder();
                    final URI requestURI = httpExchange.getRequestURI();
                    builder.withUri(requestURI);
                    final String requestMethod = httpExchange.getRequestMethod();
                    builder.withMethod(requestMethod);
                    final Headers requestHeaders = httpExchange.getRequestHeaders();
                    final HeadersBuilder headersBuilder = headersBuilder();
                    requestHeaders.forEach(headersBuilder::withAdditionalHeader);
                    builder.withHeaders(headersBuilder.build());
                    final InputStream requestBody = httpExchange.getRequestBody();
                    builder.withBody(requestBody);
                    return builder.build();
                },
                response -> {
                    final Headers responseHeaders = httpExchange.getResponseHeaders();
                    response.headers().forEach(responseHeaders::put);
                    httpExchange.sendResponseHeaders(response.status(), 0);
                    final OutputStream outputStream = httpExchange.getResponseBody();
                    response.streamBodyToOutputStream(outputStream);
                });
    }
}
