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

package de.quantummaid.httpmaid.awslambda.apigateway;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.http.async.SdkAsyncHttpClient;
import software.amazon.awssdk.services.apigatewaymanagementapi.ApiGatewayManagementApiAsyncClient;
import software.amazon.awssdk.services.apigatewaymanagementapi.ApiGatewayManagementApiAsyncClientBuilder;

import java.net.URI;

import static software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient.*;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class DefaultApiGatewayClientFactory implements ApiGatewayClientFactory {
    private final AwsCredentialsProvider credentialsProvider;
    private final SdkAsyncHttpClient httpClient;

    public static ApiGatewayClientFactory defaultApiGatewayClientFactory() {
        final AwsCredentialsProvider credentialsProvider = DefaultCredentialsProvider.create();
        final SdkAsyncHttpClient httpClient = create();
        return new DefaultApiGatewayClientFactory(credentialsProvider, httpClient);
    }

    @Override
    public ApiGatewayManagementApiAsyncClient provide(final String endpointUrl) {
        final ApiGatewayManagementApiAsyncClientBuilder apiGatewayManagementApiAsyncClientBuilder = ApiGatewayManagementApiAsyncClient.builder()
                .credentialsProvider(credentialsProvider)
                .httpClient(httpClient)
                .endpointOverride(URI.create(endpointUrl));
        return apiGatewayManagementApiAsyncClientBuilder
                .build();
    }
}
