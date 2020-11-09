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

import de.quantummaid.httpmaid.awslambda.registry.queryexecutor.QueryExecutor;
import de.quantummaid.httpmaid.awslambda.repository.Repository;
import de.quantummaid.httpmaid.websockets.criteria.WebsocketCriteria;
import de.quantummaid.httpmaid.websockets.registry.ConnectionInformation;
import de.quantummaid.httpmaid.websockets.registry.WebsocketRegistry;
import de.quantummaid.httpmaid.websockets.registry.WebsocketRegistryEntry;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.List;
import java.util.Map;

import static de.quantummaid.httpmaid.awslambda.registry.EntryDeserializer.deserializeEntry;
import static de.quantummaid.httpmaid.awslambda.registry.EntryDeserializer.serializeEntry;
import static de.quantummaid.httpmaid.awslambda.registry.queryexecutor.DefaultQueryExecutor.defaultQueryExecutor;
import static de.quantummaid.httpmaid.awslambda.repository.dynamodb.DynamoDbRepository.dynamoDbRepository;
import static de.quantummaid.httpmaid.websockets.criteria.WebsocketCriteria.websocketCriteria;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@SuppressWarnings("java:S1192")
public final class DynamoDbWebsocketRegistry implements WebsocketRegistry {
    private final Repository repository;
    private final QueryExecutor queryExecutor;

    public static DynamoDbWebsocketRegistry dynamoDbWebsocketRegistry(final Repository repository) {
        final QueryExecutor defaultQueryExecutor = defaultQueryExecutor();
        return dynamoDbWebsocketRegistry(repository, defaultQueryExecutor);
    }

    public static DynamoDbWebsocketRegistry dynamoDbWebsocketRegistry(final Repository repository,
                                                                      final QueryExecutor queryExecutor) {
        return new DynamoDbWebsocketRegistry(repository, queryExecutor);
    }

    public static DynamoDbWebsocketRegistry dynamoDbWebsocketRegistry(final String tableName,
                                                                      final String primaryKey) {
        final Repository repository = dynamoDbRepository(tableName, primaryKey);
        return dynamoDbWebsocketRegistry(repository);
    }

    @Override
    public List<WebsocketRegistryEntry> connections(final WebsocketCriteria criteria) {
        return queryExecutor.connections(criteria, repository);
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

    @Override
    public void removeConnection(final ConnectionInformation connectionInformation) {
        final String key = connectionInformation.uniqueIdentifier();
        repository.delete(key);
    }

    @Override
    public long countConnections() {
        return connections(websocketCriteria()).size();
    }
}
