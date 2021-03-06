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

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static de.quantummaid.httpmaid.http.Http.Headers.CONTENT_TYPE;
import static de.quantummaid.httpmaid.util.Validators.validateNotNull;
import static java.util.Optional.ofNullable;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class RawClientResponse {
    private final int statusCode;
    private final Map<String, List<String>> headers;
    private final InputStream content;

    public static RawClientResponse rawClientResponse(final int statusCode,
                                                      final Map<String, List<String>> headers,
                                                      final InputStream content) {
        validateNotNull(headers, "headers");
        validateNotNull(content, "content");
        return new RawClientResponse(statusCode, headers, content);
    }

    public int statusCode() {
        return statusCode;
    }

    public Map<String, List<String>> headers() {
        return headers;
    }

    public Optional<String> header(final String key) {
        return ofNullable(headers.get(key))
                .flatMap(values -> values.stream().findFirst());
    }

    public Optional<String> contentType() {
        return header(CONTENT_TYPE);
    }

    public InputStream content() {
        return content;
    }
}
