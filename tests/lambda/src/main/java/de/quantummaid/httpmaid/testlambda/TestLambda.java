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
import de.quantummaid.httpmaid.remotespecsinstance.HttpMaidFactory;
import de.quantummaid.httpmaid.websockets.registry.WebsocketRegistry;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Map;

import static de.quantummaid.httpmaid.awslambda.AwsLambdaEndpoint.awsLambdaEndpointFor;
import static de.quantummaid.httpmaid.awslambda.AwsWebsocketLambdaEndpoint.awsWebsocketLambdaEndpointFor;
import static de.quantummaid.httpmaid.awslambda.EventUtils.isWebSocketRequest;
import static de.quantummaid.httpmaid.awslambda.registry.DynamoDbWebsocketRegistry.dynamoDbWebsocketRegistry;
import static de.quantummaid.httpmaid.awslambda.repository.dynamodb.DynamoDbRepository.dynamoDbRepository;
import static de.quantummaid.httpmaid.websockets.WebsocketConfigurators.toUseWebsocketRegistry;

@ToString
@EqualsAndHashCode
public final class TestLambda {
    private static final HttpMaid HTTP_MAID = httpMaid();

    private static final AwsLambdaEndpoint PLAIN_ENDPOINT = awsLambdaEndpointFor(HTTP_MAID);
    private static final AwsWebsocketLambdaEndpoint WEBSOCKET_ENDPOINT = awsWebsocketLambdaEndpointFor(HTTP_MAID);

    private static HttpMaid httpMaid() {
        final String websocketRegistryTable = System.getenv("WEBSOCKET_REGISTRY_TABLE");
        final DynamoDbRepository dynamoDbRepository = dynamoDbRepository(websocketRegistryTable, "id");
        final WebsocketRegistry websocketRegistry = dynamoDbWebsocketRegistry(dynamoDbRepository);
        return HttpMaidFactory.httpMaid(httpMaidBuilder -> httpMaidBuilder
                .configured(toUseWebsocketRegistry(websocketRegistry)));
    }

    public Map<String, Object> handleRequest(final Map<String, Object> event) {
        if (!isWebSocketRequest(event)) {
            return PLAIN_ENDPOINT.delegate(event);
        } else {
            return WEBSOCKET_ENDPOINT.delegate(event);
        }
    }
}
