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

package de.quantummaid.httpmaid.awslambda.sender.apigateway.sync;

import de.quantummaid.httpmaid.awslambda.sender.apigateway.LowLevelFactory;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.http.SdkHttpClient;
import software.amazon.awssdk.services.apigatewaymanagementapi.ApiGatewayManagementApiClient;

import java.net.URI;

import static software.amazon.awssdk.http.apache.ApacheHttpClient.create;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@SuppressWarnings("java:S2095")
public final class DefaultApiGatewaySyncClientFactory implements LowLevelFactory<ApiGatewayManagementApiClient> {
    private final AwsCredentialsProvider credentialsProvider;
    private final SdkHttpClient httpClient;

    public static LowLevelFactory<ApiGatewayManagementApiClient> defaultApiGatewaySyncClientFactory() {
        final AwsCredentialsProvider credentialsProvider = DefaultCredentialsProvider.create();
        final SdkHttpClient httpClient = create();
        return new DefaultApiGatewaySyncClientFactory(credentialsProvider, httpClient);
    }

    @Override
    public ApiGatewayManagementApiClient provide(final String endpointUrl) {
        return ApiGatewayManagementApiClient.builder()
                .credentialsProvider(credentialsProvider)
                .httpClient(httpClient)
                .endpointOverride(URI.create(endpointUrl))
                .build();
    }

    @Override
    public void close() {
        httpClient.close();
    }
}
