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

import de.quantummaid.httpmaid.mappath.MapPath;
import de.quantummaid.httpmaid.websockets.registry.WebsocketRegistryEntry;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import static de.quantummaid.httpmaid.http.HeaderName.headerName;
import static de.quantummaid.httpmaid.http.HeaderValue.headerValue;
import static de.quantummaid.httpmaid.http.QueryParameterName.queryParameterName;
import static de.quantummaid.httpmaid.http.QueryParameterValue.queryParameterValue;
import static de.quantummaid.httpmaid.websockets.criteria.AdditionalDataStringCriterion.additionalDataStringCriterion;
import static de.quantummaid.httpmaid.websockets.criteria.HeaderCriterion.headerCriterion;
import static de.quantummaid.httpmaid.websockets.criteria.QueryParameterCriterion.queryParameterCriterion;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class WebsocketCriteria {
    private final List<HeaderCriterion> headerCriteria;
    private final List<QueryParameterCriterion> queryParameterCriteria;
    private final List<AdditionalDataStringCriterion> additionalDataStringCriteria;

    public static WebsocketCriteria websocketCriteria() {
        return new WebsocketCriteria(
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>()
        );
    }

    public WebsocketCriteria header(final String name, final String value) {
        final HeaderCriterion headerCriterion = headerCriterion(headerName(name), headerValue(value));
        headerCriteria.add(headerCriterion);
        return this;
    }

    public WebsocketCriteria queryParameter(final String name, final String value) {
        final QueryParameterCriterion queryParameterCriterion = queryParameterCriterion(
                queryParameterName(name), queryParameterValue(value)
        );
        queryParameterCriteria.add(queryParameterCriterion);
        return this;
    }

    public WebsocketCriteria additionalDataString(final String key, final String value) {
        final MapPath mapPathKey = MapPath.parse(key);
        return additionalDataString(mapPathKey, value);
    }

    public WebsocketCriteria additionalDataString(final MapPath key, final String value) {
        final AdditionalDataStringCriterion additionalDataStringCriterion = additionalDataStringCriterion(key, value);
        additionalDataStringCriteria.add(additionalDataStringCriterion);
        return this;
    }

    public boolean filter(final WebsocketRegistryEntry entry) {
        final boolean headersAreMatched = headerCriteria.stream()
                .allMatch(headerCriterion -> headerCriterion.filter(entry));
        final boolean queryParametersAreMatched = queryParameterCriteria.stream()
                .allMatch(queryParameterCriterion -> queryParameterCriterion.filter(entry));
        final boolean additionalDataIsMatched = additionalDataStringCriteria.stream()
                .allMatch(additionalDataStringCriterion -> additionalDataStringCriterion.filter(entry));
        return headersAreMatched && queryParametersAreMatched && additionalDataIsMatched;
    }

    public List<HeaderCriterion> headerCriteria() {
        return headerCriteria;
    }

    public List<QueryParameterCriterion> queryParameterCriteria() {
        return queryParameterCriteria;
    }
}
