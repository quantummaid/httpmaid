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
import de.quantummaid.httpmaid.client.body.multipart.Part;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static de.quantummaid.httpmaid.client.HeaderName.headerKey;
import static de.quantummaid.httpmaid.client.HeaderValue.headerValue;
import static de.quantummaid.httpmaid.client.HttpClientRequest.httpClientRequest;
import static de.quantummaid.httpmaid.client.QueryParameter.queryParameter;
import static de.quantummaid.httpmaid.client.body.Body.bodyWithoutContentType;
import static de.quantummaid.httpmaid.client.body.multipart.MultipartBodyCreator.createMultipartBody;
import static de.quantummaid.httpmaid.util.Validators.validateNotNull;
import static de.quantummaid.httpmaid.util.Validators.validateNotNullNorEmpty;
import static de.quantummaid.httpmaid.util.streams.Streams.stringToInputStream;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class HttpClientRequestBuilder<T> {
    private final String method;
    private final RequestPath path;
    private Body body;
    private final List<Header> headers = new ArrayList<>();
    private Class<T> targetType;

    static HttpClientRequestBuilder<SimpleHttpResponseObject> httpClientRequestBuilderImplementation(
            final String method, final String path) {
        validateNotNullNorEmpty(method, "method");
        validateNotNull(path, "path");
        final RequestPath requestPath = RequestPath.parse(path);
        final HttpClientRequestBuilder<?> httpClientRequestBuilder = new HttpClientRequestBuilder<>(method, requestPath);
        return httpClientRequestBuilder.mappedTo(SimpleHttpResponseObject.class);
    }

    public HttpClientRequestBuilder<T> withAMultipartBodyWithTheParts(final Part... parts) {
        return withTheBody(createMultipartBody(parts));
    }

    public HttpClientRequestBuilder<T> withTheBody(final String body) {
        return withTheBody(stringToInputStream(body));
    }

    public HttpClientRequestBuilder<T> withTheBody(final InputStream body) {
        return withTheBody(bodyWithoutContentType(() -> body));
    }

    public HttpClientRequestBuilder<T> withTheBody(final Body body) {
        this.body = body;
        return this;
    }

    public HttpClientRequestBuilder<T> withContentType(final String contentType) {
        return withHeader("Content-type", contentType);
    }

    public HttpClientRequestBuilder<T> withHeader(final String key, final String value) {
        final HeaderName headerName = headerKey(key);
        final HeaderValue headerValue = headerValue(value);
        final Header header = Header.header(headerName, headerValue);
        this.headers.add(header);
        return this;
    }

    public HttpClientRequestBuilder<T> withQueryParameter(final String key) {
        this.path.add(queryParameter(key));
        return this;
    }

    public HttpClientRequestBuilder<T> withQueryParameter(final String key, final String value) {
        this.path.add(queryParameter(key, value));
        return this;
    }

    public HttpClientRequestBuilder<String> mappedToString() {
        return mappedTo(String.class);
    }

    @SuppressWarnings("unchecked")
    public <X> HttpClientRequestBuilder<X> mappedTo(final Class<X> targetType) {
        this.targetType = (Class<T>) targetType;
        return (HttpClientRequestBuilder<X>) this;
    }

    HttpClientRequest<T> build(final BasePath basePath) {
        validateNotNull(basePath, "basePath");
        return httpClientRequest(path, method, headers, Optional.ofNullable(body), targetType);
    }
}
