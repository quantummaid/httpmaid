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

import java.util.*;
import java.util.stream.Collectors;

import static de.quantummaid.httpmaid.http.HeaderName.headerName;
import static de.quantummaid.httpmaid.http.HttpRequestException.httpHandlerException;
import static de.quantummaid.httpmaid.util.Validators.validateNotNull;
import static java.lang.String.format;
import static java.lang.String.join;
import static java.util.Collections.unmodifiableList;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Headers {
    private final List<Header> headers;

    public static Headers headers(final List<Header> headers) {
        validateNotNull(headers, "headers");
        return new Headers(headers);
    }

    public List<String> allValuesFor(final String name) {
        final HeaderName headerName = headerName(name);
        return headers.stream()
                .filter(header -> headerName.equals(header.name()))
                .map(Header::value)
                .map(HeaderValue::stringValue)
                .collect(Collectors.toList());
    }

    public Optional<String> optionalHeader(final String name) {
        final List<String> values = allValuesFor(name);
        if (values.isEmpty()) {
            return Optional.empty();
        }
        if (values.size() > 1) {
            final String joinedValues = join(", ", values);
            throw httpHandlerException(format("Expecting header '%s' to only have one value but got [%s]",
                    name, joinedValues));
        }
        return Optional.of(values.get(0));
    }

    public String header(final String name) {
        return optionalHeader(name)
                .orElseThrow(() -> httpHandlerException(format("No header with name '%s'", name)));
    }

    public List<Header> asList() {
        return unmodifiableList(headers);
    }

    public Map<String, List<String>> asMap() {
        final LinkedHashMap<String, List<String>> result = new LinkedHashMap<>();
        headers.forEach(header -> {
            final String name = header.name().stringValue();
            final String value = header.value().stringValue();
            final List<String> values = result.getOrDefault(name, new ArrayList<>());
            values.add(value);
            result.put(name, values);
        });
        return result;
    }
}
