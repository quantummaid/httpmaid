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
import de.quantummaid.httpmaid.util.Maps;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.quantummaid.httpmaid.endpoint.RawRequest.rawRequest;
import static de.quantummaid.httpmaid.util.streams.Streams.stringToInputStream;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class RawRequestBuilder {
    private String path;
    private String requestMethod;
    private Map<String, List<String>> headers = new HashMap<>();
    private Map<String, String> queryParameters = new HashMap<>();
    private InputStream body;
    private final Map<MetaDataKey<?>, Object> additionalMetaData = new HashMap<>();

    public static RawRequestBuilder rawRequestBuilder() {
        return new RawRequestBuilder();
    }

    public RawRequestBuilder withUri(final URI uri) {
        withPath(uri.getPath());
        withEncodedQueryParameters(uri.getQuery());
        return this;
    }

    public RawRequestBuilder withPath(final String path) {
        this.path = path;
        return this;
    }

    public RawRequestBuilder withMethod(final String method) {
        this.requestMethod = method;
        return this;
    }

    public RawRequestBuilder withUniqueHeaders(final Map<String, String> headers) {
        final Map<String, List<String>> multiMap = Maps.mapToMultiMap(headers);
        return withHeaders(multiMap);
    }

    public RawRequestBuilder withHeaders(final Map<String, List<String>> headers) {
        this.headers = headers;
        return this;
    }

    public RawRequestBuilder withEncodedQueryParameters(final String encodedQueryParameters) {
        final Map<String, String> queryParametersMap = queryToMap(encodedQueryParameters);
        return withQueryParameters(queryParametersMap);
    }

    public RawRequestBuilder withQueryParameters(final Map<String, String> queryParameters) {
        this.queryParameters = queryParameters;
        return this;
    }

    public RawRequestBuilder withBody(final String body) {
        final InputStream stream = stringToInputStream(body);
        return withBody(stream);
    }

    public RawRequestBuilder withBody(final InputStream body) {
        this.body = body;
        return this;
    }

    public <T> RawRequestBuilder withAdditionalMetaData(final MetaDataKey<T> key, final T value) {
        additionalMetaData.put(key, value);
        return this;
    }

    public RawRequest build() {
        if (body == null) {
            withBody("");
        }
        return rawRequest(path, requestMethod, headers, queryParameters, body, additionalMetaData);
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
