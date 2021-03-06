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

import de.quantummaid.httpmaid.http.QueryParameterName;
import de.quantummaid.httpmaid.http.QueryParameterValue;
import de.quantummaid.httpmaid.websockets.registry.WebsocketRegistryEntry;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.List;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class QueryParameterCriterion {
    private final QueryParameterName name;
    private final QueryParameterValue value;

    public static QueryParameterCriterion queryParameterCriterion(final QueryParameterName name,
                                                                  final QueryParameterValue value) {
        return new QueryParameterCriterion(name, value);
    }

    public boolean filter(final WebsocketRegistryEntry entry) {
        final List<String> values = entry.queryParameters()
                .allValuesFor(name.stringValue());
        return values.contains(value.stringValue());
    }

    public QueryParameterName name() {
        return name;
    }

    public QueryParameterValue value() {
        return value;
    }
}
