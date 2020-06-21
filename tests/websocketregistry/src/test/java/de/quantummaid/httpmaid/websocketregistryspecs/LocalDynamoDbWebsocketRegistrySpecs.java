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

package de.quantummaid.httpmaid.websocketregistryspecs;

import de.quantummaid.httpmaid.awslambda.registry.DynamoDbWebsocketRegistry;
import de.quantummaid.httpmaid.awslambda.repository.dynamodb.DynamoDbRepository;
import de.quantummaid.httpmaid.tests.givenwhenthen.deploy.FreePortPool;
import de.quantummaid.httpmaid.websocketregistryspecs.localdynamodb.LocalDynamoDb;
import de.quantummaid.httpmaid.websocketregistryspecs.testsupport.WebsocketRegistryDeployment;
import de.quantummaid.httpmaid.websocketregistryspecs.testsupport.WebsocketRegistryTestExtension;
import de.quantummaid.httpmaid.websockets.registry.ConnectionInformation;
import org.junit.jupiter.api.extension.ExtendWith;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import static de.quantummaid.httpmaid.awslambda.AwsWebsocketConnectionInformation.awsWebsocketConnectionInformation;
import static de.quantummaid.httpmaid.awslambda.registry.DynamoDbWebsocketRegistry.dynamoDbWebsocketRegistry;
import static de.quantummaid.httpmaid.awslambda.repository.dynamodb.DynamoDbRepository.dynamoDbRepository;

@ExtendWith(WebsocketRegistryTestExtension.class)
public final class LocalDynamoDbWebsocketRegistrySpecs implements WebsocketRegistrySpecs {
    private static final String TABLE_NAME = "websocketregistry";
    private static final String PRIMARY_KEY = "id";

    @Override
    public WebsocketRegistryDeployment websocketRegistry() {
        final int port = FreePortPool.freePort();
        final LocalDynamoDb localDynamoDb = LocalDynamoDb.startLocalDynamoDb(port);
        localDynamoDb.createTable(TABLE_NAME, PRIMARY_KEY);
        final DynamoDbClient client = localDynamoDb.client();
        final DynamoDbRepository dynamoDbRepository = dynamoDbRepository(client, TABLE_NAME, PRIMARY_KEY);
        final DynamoDbWebsocketRegistry dynamoDbWebsocketRegistry = dynamoDbWebsocketRegistry(dynamoDbRepository);
        return WebsocketRegistryDeployment.websocketRegistryDeployment(dynamoDbWebsocketRegistry, localDynamoDb::close);
    }

    @Override
    public ConnectionInformation connectionInformation() {
        return awsWebsocketConnectionInformation("a", "b", "c", "d");
    }
}
