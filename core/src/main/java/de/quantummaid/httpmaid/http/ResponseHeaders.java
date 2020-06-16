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

import de.quantummaid.httpmaid.util.Maps;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.*;

import static de.quantummaid.httpmaid.http.HeaderName.headerKey;
import static java.lang.String.format;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ResponseHeaders {
    private final Map<HeaderName, List<HeaderValue>> headers;

    public static ResponseHeaders emptyResponseHeaders() {
        return new ResponseHeaders(new LinkedHashMap<>());
    }

    public Optional<String> getOptionalHeader(final String key) {
        final HeaderName headerName = headerKey(key);
        return Maps.getOptionally(headers, headerName)
                .flatMap(headerValues -> headerValues.stream().findFirst())
                .map(HeaderValue::stringValue);
    }

    public String getHeader(final String key) {
        return getOptionalHeader(key)
                .orElseThrow(() -> new IllegalArgumentException(format("No header with name %s", key)));
    }

    public Map<String, List<String>> asStringMap() {
        return Maps.transformMap(headers, HeaderName::stringValue,
                headerValues -> headerValues.stream().map(HeaderValue::stringValue).collect(toList()));
    }

    public void addHeader(final String name, final String value) {
        final HeaderName headerName = headerKey(name);
        final List<HeaderValue> headerValues = ofNullable(headers.get(headerName)).orElse(new ArrayList<>());
        final HeaderValue headerValue = HeaderValue.headerValue(value);
        headerValues.add(headerValue);
        headers.put(headerName, headerValues);
    }

    public void setHeader(final String name, final String value) {
        final HeaderName headerName = headerKey(name);
        final HeaderValue headerValue = HeaderValue.headerValue(value);
        headers.put(headerName, new ArrayList<>(List.of(headerValue)));
    }
}
