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

import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static de.quantummaid.httpmaid.http.QueryParameterKey.queryParameterKey;
import static de.quantummaid.httpmaid.http.QueryParameterValue.queryParameterValue;
import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.emptyMap;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class QueryParameters {
    private static final QueryParameters EMPTY = new QueryParameters(emptyMap());

    private final Map<QueryParameterKey, QueryParameterValue> queryParameters;

    public static QueryParameters fromQueryString(final String queryString) {
        if (queryString == null || queryString.isEmpty()) {
            return QueryParameters.EMPTY;
        }
        final Map<QueryParameterKey, QueryParameterValue> queryMap = new HashMap<>();
        for (final String param : queryString.split("&")) {
            final String[] entry = param.split("=");
            final QueryParameterKey key = queryParameterKey(decode(entry[0]));
            final QueryParameterValue value = (entry.length > 1)
                    ? queryParameterValue(decode(entry[1])) : queryParameterValue("");
            queryMap.put(key, value);
        }
        return QueryParameters.fromQueryParameterMap(queryMap);
    }

    public static Map<String, String> queryToMap(final String query) {
        return fromQueryString(query).asStringMap();
    }

    private static String decode(String s) {
        final String decoded = URLDecoder.decode(s, UTF_8);
        return decoded;
    }

    public static QueryParameters fromStringMap(final Map<String, String> stringMap) {
        Validators.validateNotNull(stringMap, "stringMap");
        final Map<QueryParameterKey, QueryParameterValue> queryParameters = Maps.stringsToValueObjects(
                stringMap,
                QueryParameterKey::queryParameterKey,
                QueryParameterValue::queryParameterValue);
        return new QueryParameters(queryParameters);
    }

    public static QueryParameters fromQueryParameterMap(final Map<QueryParameterKey, QueryParameterValue> queryMap) {
        Validators.validateNotNull(queryMap, "queryMap");
        return new QueryParameters(queryMap);
    }

    public String getQueryParameter(final String key) {
        return getOptionalQueryParameter(key)
                .orElseThrow(() -> new RuntimeException(format("No query parameter with the key '%s'", key)));
    }

    public Optional<String> getOptionalQueryParameter(final String key) {
        final QueryParameterKey queryParameterKey = queryParameterKey(key);
        return Maps.getOptionally(queryParameters, queryParameterKey).map(QueryParameterValue::stringValue);
    }

    public Map<String, String> asStringMap() {
        return Maps.valueObjectsToStrings(queryParameters, QueryParameterKey::stringValue, QueryParameterValue::stringValue);
    }
}
