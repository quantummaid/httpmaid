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

package de.quantummaid.httpmaid.websockets.registry.filter.queryparameter;

import de.quantummaid.httpmaid.http.QueryParameter;
import de.quantummaid.httpmaid.http.QueryParameterName;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static de.quantummaid.reflectmaid.validators.NotNullValidator.validateNotNull;
import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class AllowListQueryParameterFilter implements QueryParameterFilter {
    private final List<QueryParameterName> names;

    public static QueryParameterFilter allowListQueryParameterFilter(final List<QueryParameterName> names) {
        validateNotNull(names, "names");
        return new AllowListQueryParameterFilter(names);
    }

    @Override
    public List<QueryParameter> filter(final List<QueryParameter> queryParameters) {
        return queryParameters.stream()
                .filter(queryParameter -> {
                    final QueryParameterName name = queryParameter.name();
                    return names.contains(name);
                })
                .collect(toList());
    }
}
