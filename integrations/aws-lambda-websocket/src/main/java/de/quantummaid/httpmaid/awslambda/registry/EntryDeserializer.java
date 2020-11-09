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

import de.quantummaid.httpmaid.awslambda.AwsWebsocketConnectionInformation;
import de.quantummaid.httpmaid.http.Header;
import de.quantummaid.httpmaid.http.QueryParameter;
import de.quantummaid.httpmaid.http.headers.ContentType;
import de.quantummaid.httpmaid.websockets.registry.WebsocketRegistryEntry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static de.quantummaid.httpmaid.awslambda.AwsWebsocketConnectionInformation.awsWebsocketConnectionInformation;
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

public final class EntryDeserializer {

    private EntryDeserializer() {
    }

    @SuppressWarnings("unchecked")
    public static WebsocketRegistryEntry deserializeEntry(final Map<String, Object> map) {
        final Map<String, Object> connectionInformationMap = (Map<String, Object>) map.get("connectionInformation");
        final String connectionId = (String) connectionInformationMap.get("connectionId");
        final String stage = (String) connectionInformationMap.get("stage");
        final String apiId = (String) connectionInformationMap.get("apiId");
        final String region = (String) connectionInformationMap.get("region");
        final AwsWebsocketConnectionInformation connectionInformation = awsWebsocketConnectionInformation(
                connectionId,
                stage,
                apiId,
                region
        );

        final String senderId = (String) map.get("senderId");
        final List<Map<String, String>> serializedHeaders = (List<Map<String, String>>) map.get("headers");
        final List<Header> headers = serializedHeaders.stream()
                .map(headerMap -> {
                    final String name = headerMap.get("name");
                    final String value = headerMap.get("value");
                    return header(headerName(name), headerValue(value));
                })
                .collect(toList());

        final Optional<String> contentType = Optional.ofNullable((String) map.get("contentType"));
        final List<Map<String, String>> serializedQueryParameters = (List<Map<String, String>>) map.get("queryParameters");
        final List<QueryParameter> queryParameters = serializedQueryParameters.stream()
                .map(queryParameterMap -> {
                    final String name = queryParameterMap.get("name");
                    final String value = queryParameterMap.get("value");
                    return queryParameter(queryParameterName(name), queryParameterValue(value));
                })
                .collect(toList());

        return restoreFromStrings(connectionInformation, senderId, headers(headers), contentType, queryParameters(queryParameters));
    }

    public static Map<String, Object> serializeEntry(final WebsocketRegistryEntry entry) {
        final AwsWebsocketConnectionInformation connectionInformation = (AwsWebsocketConnectionInformation) entry.connectionInformation();
        final Map<String, Object> connectionInformationMap = new HashMap<>();
        connectionInformationMap.put("connectionId", connectionInformation.connectionId);
        connectionInformationMap.put("stage", connectionInformation.stage);
        connectionInformationMap.put("apiId", connectionInformation.apiId);
        connectionInformationMap.put("region", connectionInformation.region);

        final Map<String, Object> map = new HashMap<>();
        map.put("connectionInformation", connectionInformationMap);
        map.put("senderId", entry.senderId().asString());
        final List<Map<String, String>> headers = entry.headers().asList().stream()
                .map(header -> Map.of("name", header.name().stringValue(),
                        "value", header.value().stringValue()))
                .collect(toList());
        map.put("headers", headers);

        final String contentType = encodeContentType(entry.contentType());
        map.put("contentType", contentType);

        final List<Map<String, String>> queryParameters = entry.queryParameters().asList().stream()
                .map(queryParameter -> Map.of(
                        "name", queryParameter.name().stringValue(),
                        "value", queryParameter.value().stringValue()
                ))
                .collect(toList());
        map.put("queryParameters", queryParameters);
        return map;
    }

    private static String encodeContentType(final ContentType contentType) {
        if (contentType.isEmpty()) {
            return null;
        } else {
            return contentType.internalValueForMapping();
        }
    }
}
