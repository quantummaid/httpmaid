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

package de.quantummaid.httpmaid.awslambda.sender.apigateway.async;

import de.quantummaid.httpmaid.awslambda.sender.apigateway.AbstractGatewayClient;
import de.quantummaid.httpmaid.awslambda.sender.apigateway.ApiGatewayClientFactory;
import de.quantummaid.httpmaid.awslambda.sender.apigateway.LowLevelFactory;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.apigatewaymanagementapi.ApiGatewayManagementApiAsyncClient;

import static de.quantummaid.httpmaid.awslambda.sender.apigateway.async.AsyncApiGatewayClient.asyncApiGatewayClient;
import static de.quantummaid.httpmaid.awslambda.sender.apigateway.async.DefaultApiGatewayAsyncClientFactory.defaultApiGatewayAsyncClientFactory;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ApiGatewayAsyncClientFactory implements ApiGatewayClientFactory {
    private final LowLevelFactory<ApiGatewayManagementApiAsyncClient> lowLevelFactory;

    public static ApiGatewayClientFactory defaultAsyncApiGatewayClientFactory() {
        final LowLevelFactory<ApiGatewayManagementApiAsyncClient> lowLevelFactory = defaultApiGatewayAsyncClientFactory();
        return new ApiGatewayAsyncClientFactory(lowLevelFactory);
    }

    public static ApiGatewayClientFactory asyncApiGatewayClientFactory(
            final LowLevelFactory<ApiGatewayManagementApiAsyncClient> lowLevelFactory) {
        return new ApiGatewayAsyncClientFactory(lowLevelFactory);
    }

    @Override
    public AbstractGatewayClient provide(final String endpointUrl) {
        final ApiGatewayManagementApiAsyncClient client = lowLevelFactory.provide(endpointUrl);
        return asyncApiGatewayClient(client);
    }

    @Override
    public void close() {
        lowLevelFactory.close();
    }
}
