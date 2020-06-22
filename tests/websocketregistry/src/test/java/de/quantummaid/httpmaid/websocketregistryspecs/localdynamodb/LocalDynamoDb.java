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

package de.quantummaid.httpmaid.websocketregistryspecs.localdynamodb;

import com.amazonaws.services.dynamodbv2.local.main.ServerRunner;
import com.amazonaws.services.dynamodbv2.local.server.DynamoDBProxyServer;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.net.URI;

import static de.quantummaid.httpmaid.tests.givenwhenthen.basedirectory.BaseDirectoryFinder.findProjectBaseDirectory;
import static de.quantummaid.httpmaid.websocketregistryspecs.localdynamodb.LocalDynamoDbException.localDynamoDbException;
import static java.lang.String.valueOf;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class LocalDynamoDb implements AutoCloseable {
    private static final String NATIVE_LIBS_DIRECTORY = "/tests/websocketregistry/native-libs";

    private final DynamoDBProxyServer server;
    private final DynamoDbClient client;

    public static LocalDynamoDb startLocalDynamoDb(final int port) {
        System.setProperty("sqlite4java.library.path", findProjectBaseDirectory() + NATIVE_LIBS_DIRECTORY);
        final String[] localArgs = {"-inMemory", "-port", valueOf(port)};
        try {
            final DynamoDBProxyServer server = ServerRunner.createServerFromCommandLineArgs(localArgs);
            server.start();
            final String url = String.format("http://localhost:%d", port);
            final DynamoDbClient dynamoDbClient = DynamoDbClient.builder()
                    .endpointOverride(URI.create(url))
                    .build();
            return new LocalDynamoDb(server, dynamoDbClient);
        } catch (final Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    public DynamoDbClient client() {
        return client;
    }

    public void createTable(final String name,
                            final String primaryKey) {
        final CreateTableRequest createTableRequest = CreateTableRequest.builder()
                .tableName(name)
                .keySchema(KeySchemaElement.builder()
                        .attributeName(primaryKey)
                        .keyType(KeyType.HASH)
                        .build())
                .attributeDefinitions(AttributeDefinition.builder()
                        .attributeName(primaryKey)
                        .attributeType(ScalarAttributeType.S)
                        .build())
                .provisionedThroughput(ProvisionedThroughput.builder()
                        .readCapacityUnits(1L)
                        .writeCapacityUnits(1L)
                        .build())
                .build();
        client.createTable(createTableRequest);
    }

    @Override
    public void close() {
        try {
            server.stop();
        } catch (final Exception e) {
            throw localDynamoDbException("Exception during closing of local dynamoDb instance", e);
        }
    }
}
