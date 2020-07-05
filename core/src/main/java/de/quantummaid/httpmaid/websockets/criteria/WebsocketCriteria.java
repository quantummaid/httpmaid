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

package de.quantummaid.httpmaid.websockets.criteria;

import de.quantummaid.httpmaid.websockets.registry.WebsocketRegistryEntry;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import static de.quantummaid.httpmaid.http.HeaderName.headerName;
import static de.quantummaid.httpmaid.http.HeaderValue.headerValue;
import static de.quantummaid.httpmaid.websockets.criteria.HeaderCriterion.headerCriterion;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class WebsocketCriteria {
    private final List<HeaderCriterion> headerCriteria;

    public static WebsocketCriteria websocketCriteria() {
        return new WebsocketCriteria(new ArrayList<>());
    }

    public WebsocketCriteria header(final String name, final String value) {
        final HeaderCriterion headerCriterion = headerCriterion(headerName(name), headerValue(value));
        headerCriteria.add(headerCriterion);
        return this;
    }

    public boolean filter(final WebsocketRegistryEntry entry) {
        return headerCriteria.stream()
                .allMatch(headerCriterion -> headerCriterion.filter(entry));
    }
}
