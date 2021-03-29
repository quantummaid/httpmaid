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
import de.quantummaid.httpmaid.awslambda.AwsLambdaEndpoint;
import de.quantummaid.httpmaid.awslambda.AwsWebsocketLambdaEndpoint;
import de.quantummaid.httpmaid.awslambda.repository.dynamodb.DynamoDbRepository;
import de.quantummaid.httpmaid.awslambda.sender.apigateway.ApiGatewayClientFactory;
import de.quantummaid.httpmaid.remotespecsinstance.HttpMaidFactory;
import de.quantummaid.httpmaid.websockets.registry.WebsocketRegistry;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.util.Map;

import static de.quantummaid.graalvmlambdaruntime.GraalVmLambdaRuntime.startGraalVmLambdaRuntime;
import static de.quantummaid.httpmaid.awslambda.AwsLambdaEndpoint.awsLambdaEndpointFor;
import static de.quantummaid.httpmaid.awslambda.AwsWebsocketLambdaEndpoint.awsWebsocketLambdaEndpointFor;
import static de.quantummaid.httpmaid.awslambda.EventUtils.isWebSocketRequest;
import static de.quantummaid.httpmaid.awslambda.registry.DynamoDbWebsocketRegistry.dynamoDbWebsocketRegistry;
import static de.quantummaid.httpmaid.awslambda.repository.dynamodb.DynamoDbRepository.dynamoDbRepository;
import static de.quantummaid.httpmaid.awslambda.sender.apigateway.sync.ApiGatewaySyncClientFactory.defaultSyncApiGatewayClientFactory;
import static de.quantummaid.httpmaid.lambdastructure.Structures.LAMBDA_EVENT;
import static de.quantummaid.httpmaid.websockets.WebsocketConfigurators.toUseWebsocketRegistry;

@Slf4j
public final class UnauthorizedGraalVmLambda {
    private final HttpMaid httpMaid = httpMaid();
    private final AwsLambdaEndpoint plainEndpoint = awsLambdaEndpointFor(httpMaid);
    private final AwsWebsocketLambdaEndpoint websocketLambdaEndpoint = websocketLambdaEndpoint(httpMaid);

    private static HttpMaid httpMaid() {
        final DynamoDbClient dynamoDbClient = DynamoDbClient.builder()
                .httpClient(ApacheHttpClient.create())
                .build();
        final String websocketRegistryTable = System.getenv("WEBSOCKET_REGISTRY_TABLE");
        final DynamoDbRepository dynamoDbRepository = dynamoDbRepository(dynamoDbClient, websocketRegistryTable, "id", 2.0);
        final WebsocketRegistry websocketRegistry = dynamoDbWebsocketRegistry(dynamoDbRepository);
        return HttpMaidFactory.httpMaid(httpMaidBuilder -> httpMaidBuilder
                .configured(toUseWebsocketRegistry(websocketRegistry)));
    }

    private static AwsWebsocketLambdaEndpoint websocketLambdaEndpoint(final HttpMaid httpMaid) {
        final ApiGatewayClientFactory clientFactory = defaultSyncApiGatewayClientFactory();
        final String region = System.getenv("REGION");
        return awsWebsocketLambdaEndpointFor(httpMaid, region, clientFactory);
    }

    public Map<String, Object> handleRequest(final Map<String, Object> event) {
        log.debug("new lambda event: {}", event);
        LAMBDA_EVENT.runValidation(event);
        if (!isWebSocketRequest(event)) {
            return plainEndpoint.delegate(event);
        } else {
            return websocketLambdaEndpoint.delegate(event);
        }
    }

    @SuppressWarnings("unchecked")
    public static void start() {
        final UnauthorizedGraalVmLambda unauthorizedLambda = new UnauthorizedGraalVmLambda();
        startGraalVmLambdaRuntime(map -> unauthorizedLambda.handleRequest((Map<String, Object>) map));
    }
}
