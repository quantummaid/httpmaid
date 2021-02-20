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

import de.quantummaid.httpmaid.awslambda.sender.apigateway.AbstractGatewayClient;
import de.quantummaid.httpmaid.awslambda.sender.apigateway.GatewayOperation;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.apigatewaymanagementapi.ApiGatewayManagementApiClient;
import software.amazon.awssdk.services.apigatewaymanagementapi.model.DeleteConnectionRequest;
import software.amazon.awssdk.services.apigatewaymanagementapi.model.PostToConnectionRequest;

import static de.quantummaid.httpmaid.awslambda.sender.apigateway.ApiGatewayClientUtils.deleteConnectionRequest;
import static de.quantummaid.httpmaid.awslambda.sender.apigateway.ApiGatewayClientUtils.postToConnectionRequest;
import static de.quantummaid.httpmaid.awslambda.sender.apigateway.sync.AlreadyExecutedGatewayOperation.executeSynchronouslyNow;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class SyncApiGatewayClient implements AbstractGatewayClient {
    private final ApiGatewayManagementApiClient client;

    public static SyncApiGatewayClient syncApiGatewayClient(final ApiGatewayManagementApiClient client) {
        return new SyncApiGatewayClient(client);
    }

    @Override
    public GatewayOperation sendMessage(final String connectionId,
                                        final String message) {
        final PostToConnectionRequest request = postToConnectionRequest(connectionId, message);
        return executeSynchronouslyNow(() -> client.postToConnection(request));
    }

    @Override
    public GatewayOperation disconnect(final String connectionId) {
        final DeleteConnectionRequest request = deleteConnectionRequest(connectionId);
        return executeSynchronouslyNow(() -> client.deleteConnection(request));
    }

    @Override
    public void close() {
        client.close();
    }
}
