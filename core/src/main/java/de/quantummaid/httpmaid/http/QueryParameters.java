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

import de.quantummaid.httpmaid.util.Maps;
import de.quantummaid.httpmaid.util.Validators;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import javax.management.Query;
import java.net.URLDecoder;
import java.util.*;
import java.util.stream.Collectors;

import static de.quantummaid.httpmaid.http.QueryParameter.QueryParameter;
import static de.quantummaid.httpmaid.http.QueryParameterName.queryParameterName;
import static de.quantummaid.httpmaid.http.QueryParameterValue.queryParameterValue;
import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;

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
            final QueryParameterValue value = (entry.length > 1)
                    ? queryParameterValue(decode(entry[1])) : queryParameterValue("");
            queryParameters.add(QueryParameter.QueryParameter(name, value));
        }
        return QueryParameters.queryParameters(queryParameters);
    }

    private static String decode(String s) {
        final String decoded = URLDecoder.decode(s, UTF_8);
        return decoded;
    }

    public String getQueryParameter(final String key) {
        return getOptionalQueryParameter(key)
                .orElseThrow(() -> new RuntimeException(format("No query parameter with the key '%s'", key)));
    }

    public Optional<String> getOptionalQueryParameter(final String name) {
        final QueryParameterName requestedName = queryParameterName(name);
        final List<String> found = queryParameters.stream()
                .filter(queryParameter -> queryParameter.name().equals(requestedName))
                .map(queryParameter -> queryParameter.value().stringValue())
                .collect(Collectors.toList());
        if (found.isEmpty()) {
            return Optional.empty();
        } else if (found.size() > 1) {
            throw new UnsupportedOperationException("tilt"); // TODO
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
            values.add(queryParameter.value().stringValue());
            result.put(name, values);
        });
        return result;
    }

    public List<String> allValuesFor(final String name) {
        final QueryParameterName parameterName = queryParameterName(name);
        return this.queryParameters.stream()
                .filter(parameter -> parameterName.equals(parameter.name()))
                .map(QueryParameter::value)
                .map(QueryParameterValue::stringValue)
                .collect(Collectors.toList());
    }

    public Optional<String> optionalParameter(final String name) {
        final List<String> values = allValuesFor(name);
        if (values.isEmpty()) {
            return Optional.empty();
        }
        if (values.size() > 1) {
            throw new UnsupportedOperationException("tilt"); // TODO
            // more that one exception
        }
        return Optional.of(values.get(0));
    }

    public String parameter(final String name) {
        return optionalParameter(name)
                .orElseThrow(() -> new IllegalArgumentException(format("No parameter with name %s", name)));
    }
}
