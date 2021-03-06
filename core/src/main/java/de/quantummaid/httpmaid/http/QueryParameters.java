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

import java.net.URLDecoder;
import java.util.*;
import java.util.stream.Collectors;

import static de.quantummaid.httpmaid.http.HttpRequestException.httpHandlerException;
import static de.quantummaid.httpmaid.http.QueryParameterName.queryParameterName;
import static de.quantummaid.httpmaid.http.QueryParameterValue.queryParameterValue;
import static java.lang.String.format;
import static java.lang.String.join;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class QueryParameters {
    private static final QueryParameters EMPTY = new QueryParameters(emptyList());

    private final List<QueryParameter> queryParameters;

    public static QueryParametersBuilder builder() {
        return new QueryParametersBuilder();
    }

    public static QueryParameters queryParameters(final List<QueryParameter> parameters) {
        return new QueryParameters(List.copyOf(parameters));
    }

    public static QueryParameters fromQueryString(final String queryString) {
        if (queryString == null || queryString.isEmpty()) {
            return QueryParameters.EMPTY;
        }
        final List<QueryParameter> queryParameters = new ArrayList<>();
        for (final String param : queryString.split("&")) {
            final String[] entry = param.split("=");
            final QueryParameterName name = queryParameterName(decode(entry[0]));
            final QueryParameterValue value;
            if (entry.length > 1) {
                value = queryParameterValue(decode(entry[1]));
            } else {
                value = queryParameterValue("");
            }
            queryParameters.add(QueryParameter.queryParameter(name, value));
        }
        return QueryParameters.queryParameters(queryParameters);
    }

    private static String decode(final String s) {
        return URLDecoder.decode(s, UTF_8);
    }

    public String parameter(final String name) {
        return optionalParameter(name)
                .orElseThrow(() -> httpHandlerException(format("No query parameter with the name '%s'", name)));
    }

    public Optional<String> optionalParameter(final String name) {
        final QueryParameterName requestedName = queryParameterName(name);
        final List<String> found = queryParameters.stream()
                .filter(queryParameter -> queryParameter.name().equals(requestedName))
                .map(queryParameter -> queryParameter.value().stringValue())
                .collect(Collectors.toList());
        if (found.isEmpty()) {
            return Optional.empty();
        } else if (found.size() > 1) {
            final String joinedValues = join(", ", found);
            throw httpHandlerException(format("Expecting query string parameter '%s' to only have one value but got [%s]",
                    name, joinedValues));
        } else {
            return Optional.of(found.get(0));
        }
    }

    public Map<String, List<String>> asMap() {
        final LinkedHashMap<String, List<String>> result = new LinkedHashMap<>();
        queryParameters.forEach(queryParameter -> {
            final String name = queryParameter.name().stringValue();
            final String value = queryParameter.value().stringValue();
            final List<String> values = result.getOrDefault(name, new ArrayList<>());
            values.add(value);
            result.put(name, values);
        });
        return result;
    }

    public List<QueryParameter> asList() {
        return unmodifiableList(queryParameters);
    }

    public List<String> allValuesFor(final String name) {
        final QueryParameterName parameterName = queryParameterName(name);
        return this.queryParameters.stream()
                .filter(parameter -> parameterName.equals(parameter.name()))
                .map(QueryParameter::value)
                .map(QueryParameterValue::stringValue)
                .collect(Collectors.toList());
    }
}
