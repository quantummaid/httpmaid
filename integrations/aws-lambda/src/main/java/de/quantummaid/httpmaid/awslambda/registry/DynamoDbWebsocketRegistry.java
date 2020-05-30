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
import de.quantummaid.httpmaid.awslambda.repository.Repository;
import de.quantummaid.httpmaid.http.headers.ContentType;
import de.quantummaid.httpmaid.websockets.registry.ConnectionInformation;
import de.quantummaid.httpmaid.websockets.registry.InMemoryRegistry;
import de.quantummaid.httpmaid.websockets.registry.WebsocketRegistry;
import de.quantummaid.httpmaid.websockets.registry.WebsocketRegistryEntry;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static de.quantummaid.httpmaid.awslambda.AwsWebsocketConnectionInformation.awsWebsocketConnectionInformation;
import static de.quantummaid.httpmaid.awslambda.repository.dynamodb.DynamoDbRepository.dynamoDbRepository;
import static de.quantummaid.httpmaid.websockets.registry.InMemoryRegistry.inMemoryRegistry;
import static de.quantummaid.httpmaid.websockets.registry.WebsocketRegistryEntry.restoreFromStrings;
import static java.util.stream.Collectors.toList;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class DynamoDbWebsocketRegistry implements WebsocketRegistry {
    private static final String TABLE_NAME = "WebsocketsTest";
    private final Repository repository;
    private final InMemoryRegistry inMemoryRegistry;

    public static DynamoDbWebsocketRegistry dynamoDbWebsocketRegistry() {
        final Repository repository = dynamoDbRepository(TABLE_NAME, "id");
        final InMemoryRegistry inMemoryRegistry = inMemoryRegistry();
        return new DynamoDbWebsocketRegistry(repository, inMemoryRegistry);
    }

    @Override
    public List<WebsocketRegistryEntry> connections() {
        final List<Map<String, Object>> maps = repository.loadAll();
        return maps.stream()
                .map(DynamoDbWebsocketRegistry::deserializeEntry)
                .collect(toList());
    }

    @Override
    public WebsocketRegistryEntry byConnectionInformation(final ConnectionInformation connectionInformation) {
        final String key = connectionInformation.uniqueIdentifier();
        final Map<String, Object> map = repository.load(key);
        return deserializeEntry(map);
    }

    @Override
    public void addConnection(final WebsocketRegistryEntry entry) {
        final ConnectionInformation connectionInformation = entry.connectionInformation();
        final String key = connectionInformation.uniqueIdentifier();
        final Map<String, Object> map = serializeEntry(entry);
        repository.store(key, map);
    }

    @SuppressWarnings("unchecked")
    private static WebsocketRegistryEntry deserializeEntry(final Map<String, Object> map) {
        final Map<String, Object> connectionInformationMap = (Map<String, Object>) map.get("connectionInformation");
        final String connectionId = (String) connectionInformationMap.get("connectionId");
        final String stage = (String) connectionInformationMap.get("stage");
        final String apiId = (String) connectionInformationMap.get("apiId");
        final String region = (String) connectionInformationMap.get("region");
        final AwsWebsocketConnectionInformation connectionInformation = awsWebsocketConnectionInformation(connectionId, stage, apiId, region);

        final String senderId = (String) map.get("senderId");
        final Map<String, String> headers = (Map<String, String>) map.get("headers");
        final Optional<String> contentType = Optional.ofNullable((String) map.get("contentType"));
        final Map<String, String> queryParameters = (Map<String, String>) map.get("queryParameters");

        return restoreFromStrings(connectionInformation, senderId, headers, contentType, queryParameters);
    }

    private static Map<String, Object> serializeEntry(final WebsocketRegistryEntry entry) {
        final AwsWebsocketConnectionInformation connectionInformation = (AwsWebsocketConnectionInformation) entry.connectionInformation();
        final Map<String, Object> connectionInformationMap = new HashMap<>();
        connectionInformationMap.put("connectionId", connectionInformation.connectionId);
        connectionInformationMap.put("stage", connectionInformation.stage);
        connectionInformationMap.put("apiId", connectionInformation.apiId);
        connectionInformationMap.put("region", connectionInformation.region);

        final Map<String, Object> map = new HashMap<>();
        map.put("connectionInformation", connectionInformationMap);
        map.put("senderId", entry.senderId().asString());
        map.put("headers", entry.headers().asStringMap());
        final String contentType = encodeContentType(entry.contentType());
        map.put("contentType", contentType);
        map.put("queryParameters", entry.queryParameters().asStringMap());
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
