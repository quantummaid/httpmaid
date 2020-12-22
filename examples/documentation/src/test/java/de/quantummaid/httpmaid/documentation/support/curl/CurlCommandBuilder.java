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

package de.quantummaid.httpmaid.documentation.support.curl;

import de.quantummaid.httpmaid.client.HttpClientRequestBuilder;
import de.quantummaid.httpmaid.client.SimpleHttpResponseObject;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;

import static de.quantummaid.httpmaid.client.HttpClientRequest.aRequest;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class CurlCommandBuilder {
    private String path;
    private String method = null;
    private String body = null;
    private final Map<String, String> headers = new HashMap<>();

    public static CurlCommandBuilder curlCommandBuilder() {
        return new CurlCommandBuilder();
    }

    public void setMethod(final String method) {
        this.method = method;
    }

    public void setBody(final String body) {
        this.body = body;
        if (method == null) {
            setMethod("POST");
        }
    }

    public void setPath(final String path) {
        this.path = path;
    }

    public void addHeader(final String key, final String value) {
        headers.put(key, value);
    }

    public HttpClientRequestBuilder<SimpleHttpResponseObject> build() {
        if (method == null) {
            method = "GET";
        }
        final HttpClientRequestBuilder<SimpleHttpResponseObject> builder = aRequest(method, path);
        headers.forEach(builder::withHeader);
        if (body != null) {
            builder.withTheBody(body);
        }
        return builder;
    }
}
