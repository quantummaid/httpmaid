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

package de.quantummaid.httpmaid.http;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static de.quantummaid.httpmaid.http.Header.header;
import static de.quantummaid.httpmaid.http.HeaderName.headerName;
import static de.quantummaid.httpmaid.http.HeaderValue.headerValue;
import static de.quantummaid.httpmaid.http.Headers.headers;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class HeadersBuilder {
    private final List<Header> headers;

    public static HeadersBuilder headersBuilder() {
        return new HeadersBuilder(new ArrayList<>());
    }

    public void withAdditionalHeader(final String key, final List<String> values) {
        values.forEach(value -> withAdditionalHeader(key, value));
    }

    public void withAdditionalHeader(final String key, final String value) {
        final Header header = header(headerName(key), headerValue(value));
        headers.add(header);
    }

    public void withHeadersMap(final Map<String, List<String>> map) {
        map.forEach(this::withAdditionalHeader);
    }

    public Headers build() {
        return headers(headers);
    }
}
