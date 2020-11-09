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
import de.quantummaid.httpmaid.tests.givenwhenthen.basedirectory.BaseDirectoryFinder;
import de.quantummaid.httpmaid.websocketregistryspecs.remotedynamodb.aws.cloudformation.CloudFormationHandler;
import de.quantummaid.httpmaid.websocketregistryspecs.testsupport.WebsocketRegistryDeployment;
import de.quantummaid.httpmaid.websocketregistryspecs.testsupport.WebsocketRegistryTestExtension;
import de.quantummaid.httpmaid.websockets.registry.ConnectionInformation;
import org.junit.jupiter.api.extension.ExtendWith;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.util.Map;
import java.util.UUID;

import static de.quantummaid.httpmaid.awslambda.AwsWebsocketConnectionInformation.awsWebsocketConnectionInformation;
import static de.quantummaid.httpmaid.awslambda.registry.DynamoDbWebsocketRegistry.dynamoDbWebsocketRegistry;
import static de.quantummaid.httpmaid.awslambda.repository.dynamodb.DynamoDbRepository.dynamoDbRepository;

@ExtendWith(WebsocketRegistryTestExtension.class)
public final class RemoteDynamoDbWebsocketRegistrySpecs implements WebsocketRegistrySpecs {
    private static final String STACK_PREFIX = "websocketregistryspecs-";

    private static final String CF_TEMPLATE = "/tests/websocketregistry/cf-dynamodb.yml";
    private static final String PRIMARY_KEY = "id";

    public WebsocketRegistryDeployment websocketRegistry() {
        final String stackIdentifier = STACK_PREFIX + UUID.randomUUID().toString();
        try (CloudFormationHandler cloudFormationHandler = CloudFormationHandler.connectToCloudFormation()) {
            final String projectBaseDirectory = BaseDirectoryFinder.findProjectBaseDirectory();
            final String cloudformationTemplate = projectBaseDirectory + CF_TEMPLATE;
            cloudFormationHandler.createStack(stackIdentifier,
                    cloudformationTemplate,
                    Map.of("StackIdentifier", stackIdentifier));
        }

        final DynamoDbClient dynamoDbClient = DynamoDbClient.create();
        final DynamoDbRepository dynamoDbRepository = dynamoDbRepository(dynamoDbClient, stackIdentifier, PRIMARY_KEY);
        final DynamoDbWebsocketRegistry dynamoDbWebsocketRegistry = dynamoDbWebsocketRegistry(dynamoDbRepository);

        return WebsocketRegistryDeployment.websocketRegistryDeployment(dynamoDbWebsocketRegistry, () -> {
            try (CloudFormationHandler cloudFormationHandler = CloudFormationHandler.connectToCloudFormation()) {
                cloudFormationHandler.deleteStacksStartingWith(STACK_PREFIX);
            }
        });
    }

    public ConnectionInformation connectionInformation() {
        return awsWebsocketConnectionInformation("a", "b", "c", "d");
    }
}
