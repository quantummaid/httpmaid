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

package de.quantummaid.httpmaid.awslambda.repository.dynamodb;

import de.quantummaid.httpmaid.awslambda.repository.Repository;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.LinkedHashMap;
import java.util.Map;

import static de.quantummaid.httpmaid.awslambda.repository.dynamodb.DynamoDbMarshaller.marshalTopLevelMap;
import static de.quantummaid.httpmaid.awslambda.repository.dynamodb.DynamoDbRepositoryException.dynamoDbRepositoryException;
import static de.quantummaid.httpmaid.awslambda.repository.dynamodb.DynamoDbUnmarshaller.unmarshalMap;
import static de.quantummaid.httpmaid.util.Validators.validateNotNull;

@Slf4j
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class DynamoDbRepository implements Repository {
    private static final String VALUE_IDENTIFIER = "value";
    private final DynamoDbClient dynamoDbClient;
    private final String tableName;
    private final String primaryKey;
    private final Double enforcedMaxWriteCapacityUnits;

    public static DynamoDbRepository dynamoDbRepository(final String tableName,
                                                        final String primaryKey) {
        final DynamoDbClient dynamoDbClient = DynamoDbClient.builder().build();
        return dynamoDbRepository(dynamoDbClient, tableName, primaryKey);
    }

    public static DynamoDbRepository dynamoDbRepository(final DynamoDbClient dynamoDbClient,
                                                        final String tableName,
                                                        final String primaryKey) {
        return new DynamoDbRepository(dynamoDbClient, tableName, primaryKey, null);
    }

    public static DynamoDbRepository dynamoDbRepository(final DynamoDbClient dynamoDbClient,
                                                        final String tableName,
                                                        final String primaryKey,
                                                        final Double enforcedMaxWriteCapacityUnits) {
        validateNotNull(dynamoDbClient, "dynamoDbClient");
        validateNotNull(tableName, "tableName");
        validateNotNull(primaryKey, "primaryKey");
        return new DynamoDbRepository(dynamoDbClient, tableName, primaryKey, enforcedMaxWriteCapacityUnits);
    }

    @Override
    public void store(final String key, final Map<String, Object> value) {
        final Map<String, Object> wrappedMap = Map.of(
                primaryKey, key,
                VALUE_IDENTIFIER, value
        );
        final Map<String, AttributeValue> marshalledMap = marshalTopLevelMap(wrappedMap);
        final PutItemResponse response = dynamoDbClient.putItem(builder -> {
                    builder
                            .tableName(tableName)
                            .item(marshalledMap);
                    if (enforcedMaxWriteCapacityUnits != null) {
                        builder.returnConsumedCapacity(ReturnConsumedCapacity.TOTAL);
                    }
                }
        );
        if (enforcedMaxWriteCapacityUnits != null) {
            final ConsumedCapacity consumedCapacity = response.consumedCapacity();
            final double writeCapacityUnits = consumedCapacity.capacityUnits();
            log.info("write of item {} to DynamoDB table {} consumed {} WCUs", key, tableName, writeCapacityUnits);
            if (writeCapacityUnits > enforcedMaxWriteCapacityUnits) {
                throw dynamoDbRepositoryException(
                        "write capacity units of item " + key + " in DynamoDB table " + tableName +
                                " consumed " + writeCapacityUnits +
                                " WCUs but is only allowed to consume " + enforcedMaxWriteCapacityUnits + " WCUs" +
                                " (value: " + value + ")"
                );
            }
        }
    }

    @Override
    public void delete(final String key) {
        final Map<String, AttributeValue> keyMap = marshalTopLevelMap(Map.of(primaryKey, key));
        dynamoDbClient.deleteItem(
                builder -> builder
                        .key(keyMap)
                        .tableName(tableName)
        );
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Object> load(final String key) {
        final Map<String, AttributeValue> keyMap = marshalTopLevelMap(Map.of(primaryKey, key));
        final GetItemResponse getItemResponse = dynamoDbClient.getItem(
                builder -> builder
                        .key(keyMap)
                        .tableName(tableName)
        );
        final Map<String, AttributeValue> responseItem = getItemResponse.item();
        final Map<String, Object> marshalled = unmarshalMap(responseItem);
        return (Map<String, Object>) marshalled.get(VALUE_IDENTIFIER);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Map<String, Object>> loadAll() {
        final ScanResponse scan = dynamoDbClient.scan(builder -> builder.tableName(tableName));
        final Map<String, Map<String, Object>> result = new LinkedHashMap<>();
        scan.items().stream()
                .map(DynamoDbUnmarshaller::unmarshalMap)
                .forEach(map -> {
                    final String key = (String) map.get(primaryKey);
                    final Map<String, Object> value = (Map<String, Object>) map.get(VALUE_IDENTIFIER);
                    result.put(key, value);
                });
        return result;
    }

    public DynamoDbClient dynamoDbClient() {
        return dynamoDbClient;
    }

    @Override
    public void close() {
        dynamoDbClient.close();
    }
}
