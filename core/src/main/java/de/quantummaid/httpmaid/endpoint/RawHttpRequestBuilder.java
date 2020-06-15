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

package de.quantummaid.httpmaid.endpoint;

import de.quantummaid.httpmaid.chains.MetaDataKey;
import de.quantummaid.httpmaid.http.Headers;
import de.quantummaid.httpmaid.http.QueryParameters;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.io.InputStream;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.quantummaid.httpmaid.endpoint.RawHttpRequest.rawHttpRequest;
import static de.quantummaid.httpmaid.http.QueryParameters.queryToMap;
import static de.quantummaid.httpmaid.util.streams.Streams.stringToInputStream;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class RawHttpRequestBuilder {
    private String path;
    private String requestMethod;
    private Headers headers;
    private QueryParameters queryParameters;
    private InputStream body;
    private final Map<MetaDataKey<?>, Object> additionalMetaData = new HashMap<>();

    public static RawHttpRequestBuilder rawHttpRequestBuilder() {
        return new RawHttpRequestBuilder();
    }

    public RawHttpRequestBuilder withUri(final URI uri) {
        withPath(uri.getPath());
        withEncodedQueryParameters(uri.getRawQuery());
        return this;
    }

    public RawHttpRequestBuilder withPath(final String path) {
        this.path = path;
        return this;
    }

    public RawHttpRequestBuilder withMethod(final String method) {
        this.requestMethod = method;
        return this;
    }

    public RawHttpRequestBuilder withHeaders(final Headers headers) {
        this.headers = headers;
        return this;
    }

    public RawHttpRequestBuilder withQueryParameterMap(final Map<String, ? extends Collection<String>> queryParameters) {
        final Map<String, String> uniqueQueryParameters = new HashMap<>(queryParameters.size());
        queryParameters.forEach((key, values) -> {
            final String firstValue = values.iterator().next();
            uniqueQueryParameters.put(key, firstValue);
        });
        return withUniqueQueryParameters(uniqueQueryParameters);
    }

    public RawHttpRequestBuilder withEncodedQueryParameters(final String encodedQueryParameters) {
        final QueryParameters queryParameters = QueryParameters.fromQueryString(encodedQueryParameters);
        return withQueryParameters(queryParameters);
    }

    public RawHttpRequestBuilder withUniqueQueryParameters(final Map<String, String> queryParameters) {
        throw new UnsupportedOperationException("tilt");
    }

    public RawHttpRequestBuilder withQueryParameters(final QueryParameters queryParameters) {
        this.queryParameters = queryParameters;
        return this;
    }

    public RawHttpRequestBuilder withBody(final String body) {
        final InputStream stream = stringToInputStream(body);
        return withBody(stream);
    }

    public RawHttpRequestBuilder withBody(final InputStream body) {
        this.body = body;
        return this;
    }

    public <T> RawHttpRequestBuilder withAdditionalMetaData(final MetaDataKey<T> key, final T value) {
        additionalMetaData.put(key, value);
        return this;
    }

    public RawHttpRequest build() {
        if (body == null) {
            withBody("");
        }
        return rawHttpRequest(path, requestMethod, headers, queryParameters, body, additionalMetaData);
    }
}
