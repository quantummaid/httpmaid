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

package de.quantummaid.httpmaid.awslambda.registry;

import de.quantummaid.httpmaid.http.Header;
import de.quantummaid.httpmaid.http.QueryParameter;
import de.quantummaid.httpmaid.websockets.registry.ConnectionInformation;
import de.quantummaid.httpmaid.websockets.registry.WebsocketRegistryEntry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.quantummaid.httpmaid.awslambda.AwsWebsocketSender.AWS_WEBSOCKET_SENDER;
import static de.quantummaid.httpmaid.http.Header.header;
import static de.quantummaid.httpmaid.http.HeaderName.headerName;
import static de.quantummaid.httpmaid.http.HeaderValue.headerValue;
import static de.quantummaid.httpmaid.http.Headers.headers;
import static de.quantummaid.httpmaid.http.QueryParameter.queryParameter;
import static de.quantummaid.httpmaid.http.QueryParameterName.queryParameterName;
import static de.quantummaid.httpmaid.http.QueryParameterValue.queryParameterValue;
import static de.quantummaid.httpmaid.http.QueryParameters.queryParameters;
import static de.quantummaid.httpmaid.websockets.registry.WebsocketRegistryEntry.restoreFromStrings;
import static java.util.stream.Collectors.toList;

@SuppressWarnings("java:S1192")
public final class EntryDeserializer {
    private static final String HEADERS = "h";
    private static final String NAME = "n";
    private static final String VALUE = "v";
    private static final String QUERY_PARAMETERS = "q";
    private static final String ADDITIONAL_DATA = "a";

    private EntryDeserializer() {
    }

    @SuppressWarnings("unchecked")
    public static WebsocketRegistryEntry deserializeEntry(final ConnectionInformation connectionInformation,
                                                          final Map<String, Object> map) {
        final List<Map<String, String>> serializedHeaders = (List<Map<String, String>>) map.get(HEADERS);
        final List<Header> headers = serializedHeaders.stream()
                .map(headerMap -> {
                    final String name = headerMap.get(NAME);
                    final String value = headerMap.get(VALUE);
                    return header(headerName(name), headerValue(value));
                })
                .collect(toList());
        final List<Map<String, String>> serializedQueryParameters = (List<Map<String, String>>) map.get(QUERY_PARAMETERS);
        final List<QueryParameter> queryParameters = serializedQueryParameters.stream()
                .map(queryParameterMap -> {
                    final String name = queryParameterMap.get(NAME);
                    final String value = queryParameterMap.get(VALUE);
                    return queryParameter(queryParameterName(name), queryParameterValue(value));
                })
                .collect(toList());
        final Map<String, Object> additionalData = (Map<String, Object>) map.get(ADDITIONAL_DATA);
        final String senderId = AWS_WEBSOCKET_SENDER.asString();
        return restoreFromStrings(connectionInformation, senderId, headers(headers), queryParameters(queryParameters), additionalData);
    }

    public static Map<String, Object> serializeEntry(final WebsocketRegistryEntry entry) {
        final Map<String, Object> map = new HashMap<>();
        final List<Map<String, String>> headers = entry.headers().asList().stream()
                .map(header -> Map.of(NAME, header.name().stringValue(),
                        VALUE, header.value().stringValue()))
                .collect(toList());
        map.put(HEADERS, headers);

        final List<Map<String, String>> queryParameters = entry.queryParameters().asList().stream()
                .map(queryParameter -> Map.of(
                        NAME, queryParameter.name().stringValue(),
                        VALUE, queryParameter.value().stringValue()
                ))
                .collect(toList());
        map.put(QUERY_PARAMETERS, queryParameters);
        map.put(ADDITIONAL_DATA, entry.additionalData());
        return map;
    }
}
