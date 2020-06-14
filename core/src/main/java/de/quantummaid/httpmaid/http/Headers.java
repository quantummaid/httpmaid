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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static de.quantummaid.httpmaid.http.HeaderKey.headerKey;
import static de.quantummaid.httpmaid.util.Validators.validateNotNull;
import static java.lang.String.format;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Headers {
    private final List<Header> headers;

    public static Headers headers(final List<Header> headers) {
        validateNotNull(headers, "headers");
        return new Headers(headers);
    }

    public List<String> allValuesFor(final String key) {
        final HeaderKey headerKey = headerKey(key);
        return headers.stream()
                .filter(header -> headerKey.equals(header.key()))
                .map(Header::value)
                .map(HeaderValue::stringValue)
                .collect(Collectors.toList());
    }

    public Optional<String> optionalHeader(final String key) {
        final List<String> values = allValuesFor(key);
        if (values.isEmpty()) {
            return Optional.empty();
        }
        if (values.size() > 1) {
            throw new UnsupportedOperationException("tilt"); // TODO
            // more that one exception
        }
        return Optional.of(values.get(0));
    }

    public String header(final String key) {
        return optionalHeader(key)
                .orElseThrow(() -> new IllegalArgumentException(format("No header with name %s", key)));
    }
}
