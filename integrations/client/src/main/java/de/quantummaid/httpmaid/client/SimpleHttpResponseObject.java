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

import de.quantummaid.httpmaid.util.describing.MapDumper;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.lang.String.valueOf;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class SimpleHttpResponseObject {
    private final int statusCode;
    private final Map<String, List<String>> headers;
    private final String body;

    public static SimpleHttpResponseObject httpClientResponse(final int statusCode,
                                                              final Map<String, List<String>> headers,
                                                              final String body) {
        return new SimpleHttpResponseObject(statusCode, headers, body);
    }

    public int getStatusCode() {
        return statusCode;
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    public String getBody() {
        return body;
    }

    public String describe() {
        final Map<String, Object> map = Map.of("Status Code", valueOf(statusCode),
                "Headers", headers,
                "Body", body);
        return MapDumper.describe("HTTP Response", map);
    }

    public String getSingleHeader(final String headerName) {
        return getOptionalSingleHeader(headerName)
                .orElseThrow(() -> new IllegalArgumentException(String.format("No header with name '%s'", headerName)));
    }

    public Optional<String> getOptionalSingleHeader(final String headerName) {
        final List<String> headerValues = headers.get(headerName);
        if (headerValues == null) {
            return Optional.empty();
        }
        if (headerValues.size() != 1) {
            throw new UnsupportedOperationException(
                    String.format("getSingleHeader('%s') expects a single header, found %d instead (%s)",
                            headerName, headerValues.size(), headerValues));
        }
        return Optional.of(headerValues.get(0));
    }
}
