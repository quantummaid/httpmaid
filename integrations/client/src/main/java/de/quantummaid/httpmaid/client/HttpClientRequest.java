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

package de.quantummaid.httpmaid.client;

import de.quantummaid.httpmaid.client.body.Body;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static de.quantummaid.httpmaid.client.HeaderValue.headerValue;
import static de.quantummaid.httpmaid.http.Http.Headers.CONTENT_TYPE;
import static java.util.Collections.unmodifiableList;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class HttpClientRequest<T> {
    private final RequestPath path;
    private final String method;
    private final List<Header> headers;
    private final InputStream body;
    private final Class<T> targetType;

    public static HttpClientRequestBuilder<SimpleHttpResponseObject> aGetRequestToThePath(final String path) {
        return aRequest("GET", path);
    }

    public static HttpClientRequestBuilder<SimpleHttpResponseObject> aPostRequestToThePath(final String path) {
        return aRequest("POST", path);
    }

    public static HttpClientRequestBuilder<SimpleHttpResponseObject> aPutRequestToThePath(final String path) {
        return aRequest("PUT", path);
    }

    public static HttpClientRequestBuilder<SimpleHttpResponseObject> aDeleteRequestToThePath(final String path) {
        return aRequest("DELETE", path);
    }

    public static HttpClientRequestBuilder<SimpleHttpResponseObject> anOptionsRequestToThePath(final String path) {
        return aRequest("OPTIONS", path);
    }

    public static HttpClientRequestBuilder<SimpleHttpResponseObject> aRequest(final String method, final String path) {
        return HttpClientRequestBuilder.httpClientRequestBuilderImplementation(method, path);
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    static <T> HttpClientRequest<T> httpClientRequest(
            final RequestPath requestPath,
            final String method,
            final List<Header> headers,
            final Optional<Body> bodyOptional,
            final Class<T> targetType) {
        final List<Header> fixedHeaders = new ArrayList<>(headers);
        final InputStream bodyStream;
        if (bodyOptional.isPresent()) {
            final Body body = bodyOptional.get();
            body.contentType().ifPresent(contentType ->
                    fixedHeaders.add(Header.header(HeaderName.headerKey(CONTENT_TYPE), headerValue(contentType)))
            );
            bodyStream = body.inputStream();
        } else {
            bodyStream = null;
        }

        return new HttpClientRequest<>(requestPath, method, fixedHeaders, bodyStream, targetType);
    }

    public RequestPath path(final BasePath basePath) {
        return path.rebase(basePath);
    }

    public String method() {
        return this.method;
    }

    public List<Header> headers() {
        return unmodifiableList(headers);
    }

    public Optional<InputStream> body() {
        return Optional.ofNullable(this.body);
    }

    Class<T> targetType() {
        return targetType;
    }
}
