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

package de.quantummaid.httpmaid.events.enriching.enrichers;

import de.quantummaid.httpmaid.handler.http.HttpRequest;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Optional;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class HeaderEnricher implements TopEnricher {
    private final String headerName;
    private final String mapKey;

    public static HeaderEnricher headerEnricher(final String headerName,
                                                final String mapKey) {
        return new HeaderEnricher(headerName, mapKey);
    }

    @Override
    public String mapKey() {
        return mapKey;
    }

    @Override
    public Optional<String> extractValue(final HttpRequest request) {
        return request.headers()
                .optionalHeader(headerName);
    }
}
