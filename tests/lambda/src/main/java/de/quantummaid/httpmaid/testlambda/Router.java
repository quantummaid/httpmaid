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
import de.quantummaid.httpmaid.awslambda.AwsLambdaEvent;
import de.quantummaid.httpmaid.awslambda.AwsWebsocketLambdaEndpoint;
import de.quantummaid.httpmaid.awslambda.repository.dynamodb.DynamoDbRepository;
import de.quantummaid.httpmaid.awslambdacognitoauthorizer.LambdaAuthorizer;
import de.quantummaid.httpmaid.remotespecsinstance.HttpMaidFactory;
import de.quantummaid.httpmaid.websockets.registry.WebsocketRegistry;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import static de.quantummaid.httpmaid.awslambda.AwsLambdaEndpoint.awsLambdaEndpointFor;
import static de.quantummaid.httpmaid.awslambda.AwsLambdaEvent.AWS_LAMBDA_EVENT;
import static de.quantummaid.httpmaid.awslambda.AwsWebsocketLambdaEndpoint.awsWebsocketLambdaEndpointFor;
import static de.quantummaid.httpmaid.awslambda.EventUtils.isAuthorizationRequest;
import static de.quantummaid.httpmaid.awslambda.EventUtils.isWebSocketRequest;
import static de.quantummaid.httpmaid.awslambda.registry.DynamoDbWebsocketRegistry.dynamoDbWebsocketRegistry;
import static de.quantummaid.httpmaid.awslambda.repository.dynamodb.DynamoDbRepository.dynamoDbRepository;
import static de.quantummaid.httpmaid.websockets.WebsocketConfigurators.toUseWebsocketRegistry;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class Router {
    private final AwsLambdaEndpoint httpEndpoint;
    private final AwsWebsocketLambdaEndpoint websocketEndpoint;
    private final LambdaAuthorizer authorizer;

    public static Router router(final LambdaAuthorizer authorizer) {
        final String region = System.getenv("REGION");
        final String websocketRegistryTable = System.getenv("WEBSOCKET_REGISTRY_TABLE");
        final DynamoDbRepository dynamoDbRepository = dynamoDbRepository(websocketRegistryTable, "id");
        final WebsocketRegistry websocketRegistry = dynamoDbWebsocketRegistry(dynamoDbRepository);
        final HttpMaid httpMaid = HttpMaidFactory.httpMaid(httpMaidBuilder -> httpMaidBuilder
                .websocket("returnLambdaContext", (request, response) -> {
                    final AwsLambdaEvent awsLambdaEvent = request.getMetaData().get(AWS_LAMBDA_EVENT);
                    final String foo = awsLambdaEvent.getMap("requestContext")
                            .getMap("authorizer")
                            .getAsString("foo");
                    response.setBody(foo);
                })
                .configured(toUseWebsocketRegistry(websocketRegistry)));
        final AwsLambdaEndpoint httpEndpoint = awsLambdaEndpointFor(httpMaid);
        final AwsWebsocketLambdaEndpoint websocketEndpoint = awsWebsocketLambdaEndpointFor(httpMaid, region);
        return new Router(httpEndpoint, websocketEndpoint, authorizer);
    }

    public Map<String, Object> route(final Map<String, Object> event) {
        log.debug("new lambda event: {}", event);
        if (isAuthorizationRequest(event)) {
            return authorizer.delegate(event);
        } else if (!isWebSocketRequest(event)) {
            return httpEndpoint.delegate(event);
        } else {
            return websocketEndpoint.delegate(event);
        }
    }
}
