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
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.List;
import java.util.Map;

import static de.quantummaid.httpmaid.awslambda.repository.dynamodb.DynamoDbMarshaller.marshalTopLevelMap;
import static de.quantummaid.httpmaid.awslambda.repository.dynamodb.DynamoDbUnmarshaller.unmarshallMap;
import static java.util.stream.Collectors.toList;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class DynamoDbRepository implements Repository {
    private static final String VALUE_IDENTIFIER = "value";
    private final DynamoDbClient dynamoDbClient;
    private final String tableName;
    private final String primaryKey;

    public static DynamoDbRepository dynamoDbRepository(final String tableName,
                                                        final String primaryKey) {
        final DynamoDbClient dynamoDbClient = DynamoDbClient.builder().build();
        return new DynamoDbRepository(dynamoDbClient, tableName, primaryKey);
    }

    @Override
    public void store(final String key, final Map<String, Object> value) {
        final Map<String, Object> wrappedMap = Map.of(
                primaryKey, key,
                VALUE_IDENTIFIER, value
        );
        final Map<String, AttributeValue> marshalledMap = marshalTopLevelMap(wrappedMap);
        final Put put = Put.builder()
                .tableName(tableName)
                .item(marshalledMap)
                .build();
        final TransactWriteItem transactWriteItem = TransactWriteItem.builder()
                .put(put)
                .build();
        final TransactWriteItemsRequest transactionWriteRequest = TransactWriteItemsRequest.builder()
                .transactItems(List.of(transactWriteItem))
                .build();
        dynamoDbClient.transactWriteItems(transactionWriteRequest);
    }

    @Override
    public void delete(final String key) {
        final Map<String, AttributeValue> keyMap = marshalTopLevelMap(Map.of(primaryKey, key));
        final DeleteItemRequest deleteItemRequest = DeleteItemRequest.builder()
                .key(keyMap)
                .tableName(tableName)
                .build();
        dynamoDbClient.deleteItem(deleteItemRequest);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Object> load(final String key) {
        final Map<String, AttributeValue> keyMap = marshalTopLevelMap(Map.of(primaryKey, key));
        final GetItemRequest getItemRequest = GetItemRequest.builder()
                .key(keyMap)
                .tableName(tableName)
                .build();
        final GetItemResponse getItemResponse = dynamoDbClient.getItem(getItemRequest);
        final Map<String, AttributeValue> responseItem = getItemResponse.item();
        final Map<String, Object> marshalled = unmarshallMap(responseItem);
        return (Map<String, Object>) marshalled.get("value");
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Map<String, Object>> loadAll() {
        final ScanRequest scanRequest = ScanRequest.builder()
                .tableName(tableName)
                .build();
        final ScanResponse scan = dynamoDbClient.scan(scanRequest);
        return scan.items().stream()
                .map(DynamoDbUnmarshaller::unmarshallMap)
                .map(map -> map.get(VALUE_IDENTIFIER))
                .map(object -> (Map<String, Object>) object)
                .collect(toList());
    }

    @Override
    public void close() {
        dynamoDbClient.close();
    }
}
