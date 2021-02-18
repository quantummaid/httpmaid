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

package de.quantummaid.httpmaid.testlambda;

import de.quantummaid.httpmaid.HttpMaid;
import de.quantummaid.httpmaid.HttpMaidBuilder;
import de.quantummaid.httpmaid.awslambda.AwsLambdaEndpoint;
import de.quantummaid.httpmaid.awslambda.AwsWebsocketLambdaEndpoint;
import de.quantummaid.httpmaid.awslambda.authorizer.LambdaWebsocketAuthorizer;
import de.quantummaid.httpmaid.awslambda.repository.dynamodb.DynamoDbRepository;
import de.quantummaid.httpmaid.remotespecsinstance.HttpMaidFactory;
import de.quantummaid.httpmaid.websockets.registry.WebsocketRegistry;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.util.Map;
import java.util.function.Consumer;

import static de.quantummaid.httpmaid.awslambda.AwsLambdaEndpoint.awsLambdaEndpointFor;
import static de.quantummaid.httpmaid.awslambda.AwsWebsocketLambdaEndpoint.awsWebsocketLambdaEndpointFor;
import static de.quantummaid.httpmaid.awslambda.EventUtils.isAuthorizationRequest;
import static de.quantummaid.httpmaid.awslambda.EventUtils.isWebSocketRequest;
import static de.quantummaid.httpmaid.awslambda.authorizer.LambdaWebsocketAuthorizer.lambdaWebsocketAuthorizer;
import static de.quantummaid.httpmaid.awslambda.registry.DynamoDbWebsocketRegistry.dynamoDbWebsocketRegistry;
import static de.quantummaid.httpmaid.awslambda.repository.dynamodb.DynamoDbRepository.dynamoDbRepository;
import static de.quantummaid.httpmaid.websockets.WebsocketConfigurators.toUseWebsocketRegistry;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class Router {
    private final AwsLambdaEndpoint httpEndpoint;
    private final AwsWebsocketLambdaEndpoint websocketEndpoint;
    private final LambdaWebsocketAuthorizer authorizer;

    public static Router router(final Consumer<HttpMaidBuilder> additionalConfig) {
        final String region = System.getenv("REGION");
        final String websocketRegistryTable = System.getenv("WEBSOCKET_REGISTRY_TABLE");
        final DynamoDbClient dynamoDbClient = DynamoDbClient.builder().build();
        final DynamoDbRepository dynamoDbRepository = dynamoDbRepository(dynamoDbClient, websocketRegistryTable, "id", 2.0);
        final WebsocketRegistry websocketRegistry = dynamoDbWebsocketRegistry(dynamoDbRepository);
        final HttpMaid httpMaid = HttpMaidFactory.httpMaid(httpMaidBuilder -> {
            additionalConfig.accept(httpMaidBuilder);
            httpMaidBuilder.configured(toUseWebsocketRegistry(websocketRegistry));
        });
        final AwsLambdaEndpoint httpEndpoint = awsLambdaEndpointFor(httpMaid);
        final AwsWebsocketLambdaEndpoint websocketEndpoint = awsWebsocketLambdaEndpointFor(httpMaid, region);
        final LambdaWebsocketAuthorizer authorizer = lambdaWebsocketAuthorizer(httpMaid);
        return new Router(httpEndpoint, websocketEndpoint, authorizer);
    }

    public Map<String, Object> route(final Map<String, Object> event) {
        if (isAuthorizationRequest(event)) {
            return authorizer.delegate(event);
        } else if (!isWebSocketRequest(event)) {
            return httpEndpoint.delegate(event);
        } else {
            return websocketEndpoint.delegate(event);
        }
    }
}
