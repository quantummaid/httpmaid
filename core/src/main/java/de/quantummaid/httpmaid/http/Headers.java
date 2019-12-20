/*
 * Copyright (c) 2019 Richard Hauswald - https://quantummaid.de/.
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
import de.quantummaid.httpmaid.util.Validators;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.lang.String.format;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Headers {
    private final Map<HeaderKey, HeaderValue> headers;

    public static Headers headers(final Map<String, List<String>> stringMap) {
        Validators.validateNotNull(stringMap, "stringMap");
        final Map<HeaderKey, HeaderValue> headers = Maps.transformMap(stringMap, HeaderKey::headerKey, HeaderValue::headerValue);
        return new Headers(headers);
    }

    public Optional<String> getOptionalHeader(final String key) {
        final HeaderKey headerKey = HeaderKey.headerKey(key);
        return Maps.getOptionally(headers, headerKey)
                .map(HeaderValue::stringValue);
    }

    public String getHeader(final String key) {
        return getOptionalHeader(key)
                .orElseThrow(() -> new IllegalArgumentException(format("No header with name %s", key)));
    }

    public Map<String, String> asStringMap() {
        return Maps.valueObjectsToStrings(headers, HeaderKey::stringValue, HeaderValue::stringValue);
    }
}
