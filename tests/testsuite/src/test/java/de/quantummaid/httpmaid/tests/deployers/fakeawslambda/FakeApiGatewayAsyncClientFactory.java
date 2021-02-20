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

package de.quantummaid.httpmaid.tests.deployers.fakeawslambda;

import de.quantummaid.httpmaid.awslambda.sender.apigateway.LowLevelFactory;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.apigatewaymanagementapi.ApiGatewayManagementApiAsyncClient;

import java.net.URI;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class FakeApiGatewayAsyncClientFactory implements LowLevelFactory<ApiGatewayManagementApiAsyncClient> {
    private final int port;

    public static LowLevelFactory<ApiGatewayManagementApiAsyncClient> fakeApiGatewayClientFactory(final int port) {
        return new FakeApiGatewayAsyncClientFactory(port);
    }

    @Override
    public ApiGatewayManagementApiAsyncClient provide(final String endpointUrl) {
        CredentialsFixer.fixCredentials();
        final String uri = String.format("http://localhost:%d/", port);
        return ApiGatewayManagementApiAsyncClient.builder()
                .endpointOverride(URI.create(uri))
                .build();
    }
}
