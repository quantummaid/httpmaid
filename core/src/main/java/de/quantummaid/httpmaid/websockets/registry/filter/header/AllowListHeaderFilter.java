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

package de.quantummaid.httpmaid.websockets.registry.filter.header;

import de.quantummaid.httpmaid.http.Header;
import de.quantummaid.httpmaid.http.HeaderName;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static de.quantummaid.httpmaid.http.Http.Headers.ACCEPT;
import static de.quantummaid.httpmaid.http.Http.Headers.CONTENT_TYPE;
import static de.quantummaid.httpmaid.util.Validators.validateNotNull;
import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class AllowListHeaderFilter implements HeaderFilter {
    private static final List<HeaderName> ALWAYS_ALLOWED = List.of(
            HeaderName.headerName(CONTENT_TYPE),
            HeaderName.headerName(ACCEPT)
    );

    private final List<HeaderName> names;

    public static HeaderFilter onlyAllowingDefaultHeaders() {
        return allowListHeaderFilter(Collections.emptyList());
    }

    public static HeaderFilter allowListHeaderFilter(final List<HeaderName> names) {
        validateNotNull(names, "names");
        final List<HeaderName> allAllowedHeaders = new ArrayList<>(names);
        ALWAYS_ALLOWED.forEach(headerName -> {
            if (!allAllowedHeaders.contains(headerName)) {
                allAllowedHeaders.add(headerName);
            }
        });
        return new AllowListHeaderFilter(allAllowedHeaders);
    }

    @Override
    public List<Header> filter(final List<Header> headers) {
        return headers.stream()
                .filter(header -> {
                    final HeaderName name = header.name();
                    return names.contains(name);
                })
                .collect(toList());
    }
}
