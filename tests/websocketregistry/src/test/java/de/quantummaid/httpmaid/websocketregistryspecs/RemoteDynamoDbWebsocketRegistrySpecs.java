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
import de.quantummaid.httpmaid.awslambda.repository.dynamodb.DynamoDbRepositoryException;
import de.quantummaid.httpmaid.tests.givenwhenthen.basedirectory.BaseDirectoryFinder;
import de.quantummaid.httpmaid.websocketregistryspecs.remotedynamodb.aws.cloudformation.CloudFormationHandler;
import de.quantummaid.httpmaid.websocketregistryspecs.testsupport.WebsocketRegistryDeployment;
import de.quantummaid.httpmaid.websocketregistryspecs.testsupport.WebsocketRegistryTestExtension;
import de.quantummaid.httpmaid.websockets.registry.ConnectionInformation;
import de.quantummaid.httpmaid.websockets.registry.WebsocketRegistry;
import de.quantummaid.httpmaid.websockets.registry.WebsocketRegistryEntry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static de.quantummaid.httpmaid.awslambda.AwsWebsocketConnectionInformation.awsWebsocketConnectionInformation;
import static de.quantummaid.httpmaid.awslambda.registry.DynamoDbWebsocketRegistry.dynamoDbWebsocketRegistry;
import static de.quantummaid.httpmaid.awslambda.repository.dynamodb.DynamoDbRepository.dynamoDbRepository;
import static de.quantummaid.httpmaid.http.Header.header;
import static de.quantummaid.httpmaid.http.HeaderName.headerName;
import static de.quantummaid.httpmaid.http.HeaderValue.headerValue;
import static de.quantummaid.httpmaid.http.Headers.headers;
import static de.quantummaid.httpmaid.http.QueryParameter.queryParameter;
import static de.quantummaid.httpmaid.http.QueryParameterName.queryParameterName;
import static de.quantummaid.httpmaid.http.QueryParameterValue.queryParameterValue;
import static de.quantummaid.httpmaid.http.QueryParameters.queryParameters;
import static de.quantummaid.httpmaid.websockets.registry.WebsocketRegistryEntry.websocketRegistryEntry;
import static de.quantummaid.httpmaid.websockets.sender.WebsocketSenderId.websocketSenderId;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

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
        final DynamoDbRepository dynamoDbRepository = dynamoDbRepository(dynamoDbClient, stackIdentifier, PRIMARY_KEY, 2.0);
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

    @Test
    public void maxWriteCapacityUnitsCanBeEnforced(final WebsocketRegistry websocketRegistry) {
        final String largeString = "abc".repeat(1000);
        final ConnectionInformation connectionInformation = connectionInformation();
        final WebsocketRegistryEntry entry = websocketRegistryEntry(
                connectionInformation,
                websocketSenderId("foo"),
                headers(List.of(header(headerName("header-name"), headerValue(largeString)))),
                queryParameters(List.of(
                        queryParameter(queryParameterName("query-name"),
                                queryParameterValue("query-value"))
                        )
                ),
                Map.of("a", "b")
        );
        DynamoDbRepositoryException exception = null;
        try {
            websocketRegistry.addConnection(entry);
        } catch (final DynamoDbRepositoryException e) {
            exception = e;
        }
        assertThat(exception, is(notNullValue()));
        final String message = exception.getMessage();
        assertThat(message, containsString("write capacity units of item a/b/c/d " +
                "in DynamoDB table websocketregistryspecs-"));
        assertThat(message, containsString("consumed 4.0 WCUs but is only allowed to consume 2.0 WCUs"));
    }
}
