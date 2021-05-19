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

package de.quantummaid.httpmaid.remotespecs.lambda.aws.dynamodb;

import de.quantummaid.mapmaid.mapper.marshalling.Unmarshaller;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static de.quantummaid.httpmaid.tests.givenwhenthen.Poller.pollWithTimeout;
import static de.quantummaid.httpmaid.util.Validators.validateNotNull;
import static de.quantummaid.mapmaid.dynamodb.attributevalue.AttributeValueUnmarshaller.attributeValueUnmarshaller;

@Slf4j
public final class DynamoDbHandler {
    private static final int MAX_NUMBER_OF_TRIES = 10;
    private static final int SLEEP_TIME_IN_MILLISECONDS = 1000;
    private static final String PARTITION_KEY = "id";
    private static final Unmarshaller<AttributeValue> UNMARSHALLER = attributeValueUnmarshaller();

    private DynamoDbHandler() {
    }

    public static List<Map<String, Object>> entries(final String tableName) {
        try (DynamoDbClient dynamoDbClient = DynamoDbClient.create()) {
            final ScanRequest scanRequest = ScanRequest.builder()
                    .tableName(tableName)
                    .build();
            final ScanResponse scan = dynamoDbClient.scan(scanRequest);
            return scan.items().stream()
                    .map(map -> {
                        try {
                            final AttributeValue marshalledKey = map.get(PARTITION_KEY);
                            final String key = (String) UNMARSHALLER.unmarshal(marshalledKey);
                            final AttributeValue marshalledValue = map.get("value");
                            final Object value = UNMARSHALLER.unmarshal(marshalledValue);
                            return Map.of(key, value);
                        } catch (final Exception e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .collect(Collectors.toList());
        }
    }

    public static void resetTable(final String tableName) {
        log.info("Resetting table {}...", tableName);
        validateNotNull(tableName, "tableName");
        try (DynamoDbClient dynamoDbClient = DynamoDbClient.create()) {
            final ScanRequest scanRequest = ScanRequest.builder()
                    .tableName(tableName)
                    .build();
            final ScanResponse scan = dynamoDbClient.scan(scanRequest);
            scan.items().forEach(item -> {
                final AttributeValue id = item.get(PARTITION_KEY);
                delete(id, tableName, dynamoDbClient);
            });
        }
        log.info("Table {} has been successfully reset.", tableName);
    }

    private static void delete(final AttributeValue id,
                               final String tableName,
                               final DynamoDbClient dynamoDbClient) {
        final Map<String, AttributeValue> keyMap = Map.of(PARTITION_KEY, id);
        final DeleteItemRequest deleteItemRequest = DeleteItemRequest.builder()
                .key(keyMap)
                .tableName(tableName)
                .build();
        dynamoDbClient.deleteItem(deleteItemRequest);
    }

    public static void createTable(final String name) {
        log.info("Creating table {}...", name);
        try (DynamoDbClient dynamoDbClient = DynamoDbClient.create()) {
            final CreateTableRequest createTableRequest = CreateTableRequest.builder()
                    .tableName(name)
                    .keySchema(KeySchemaElement.builder()
                            .attributeName(PARTITION_KEY)
                            .keyType(KeyType.HASH)
                            .build())
                    .attributeDefinitions(AttributeDefinition.builder()
                            .attributeName(PARTITION_KEY)
                            .attributeType(ScalarAttributeType.S)
                            .build())
                    .billingMode(BillingMode.PAY_PER_REQUEST)
                    .build();
            dynamoDbClient.createTable(createTableRequest);
            pollWithTimeout(MAX_NUMBER_OF_TRIES, SLEEP_TIME_IN_MILLISECONDS, () -> {
                final boolean tableExists = tableExists(name, dynamoDbClient);
                if (!tableExists) {
                    log.info("Table {} does not exist yet - waiting...", name);
                }
                return tableExists;
            });
        }
        log.info("Table {} successfully created.", name);
    }

    public static void deleteTable(final String name) {
        log.info("Deleting table {}...", name);
        try (DynamoDbClient dynamoDbClient = DynamoDbClient.create()) {
            dynamoDbClient.deleteTable(DeleteTableRequest.builder()
                    .tableName(name)
                    .build());
            pollWithTimeout(MAX_NUMBER_OF_TRIES, SLEEP_TIME_IN_MILLISECONDS, () -> {
                final boolean tableExists = tableExists(name, dynamoDbClient);
                if (tableExists) {
                    log.info("Table {} still exists - waiting...", name);
                }
                return !tableExists;
            });
        }
        log.info("Table {} successfully deleted.", name);
    }

    private static boolean tableExists(final String name,
                                       final DynamoDbClient dynamoDbClient) {
        final ListTablesResponse listTablesResponse = dynamoDbClient.listTables();
        return listTablesResponse.tableNames().contains(name);
    }
}
