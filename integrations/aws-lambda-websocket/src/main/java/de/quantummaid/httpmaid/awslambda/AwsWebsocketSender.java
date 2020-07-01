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

package de.quantummaid.httpmaid.awslambda;

import de.quantummaid.httpmaid.awslambda.apigateway.ApiGatewayClientFactory;
import de.quantummaid.httpmaid.websockets.sender.WebsocketSender;
import de.quantummaid.httpmaid.websockets.sender.WebsocketSenderId;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.apigatewaymanagementapi.ApiGatewayManagementApiClient;
import software.amazon.awssdk.services.apigatewaymanagementapi.model.DeleteConnectionRequest;
import software.amazon.awssdk.services.apigatewaymanagementapi.model.PostToConnectionRequest;

import static de.quantummaid.httpmaid.websockets.sender.WebsocketSenderId.websocketSenderId;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class AwsWebsocketSender implements WebsocketSender<AwsWebsocketConnectionInformation> {
    public static final WebsocketSenderId AWS_WEBSOCKET_SENDER = websocketSenderId("AWS_WEBSOCKET_SENDER");

    private final ApiGatewayClientFactory clientFactory;

    public static AwsWebsocketSender awsWebsocketSender(final ApiGatewayClientFactory clientFactory) {
        return new AwsWebsocketSender(clientFactory);
    }

    @Override
    public void send(final AwsWebsocketConnectionInformation connectionInformation,
                     final String message) {
        try (ApiGatewayManagementApiClient apiGatewayManagementApiClient = clientFactory.provide(connectionInformation)) {
            final String connectionId = connectionInformation.connectionId;
            final PostToConnectionRequest request = PostToConnectionRequest.builder()
                    .connectionId(connectionId)
                    .data(SdkBytes.fromUtf8String(message))
                    .build();
            apiGatewayManagementApiClient.postToConnection(request);
        }
    }

    @Override
    public void disconnect(final AwsWebsocketConnectionInformation connectionInformation) {
        try (ApiGatewayManagementApiClient apiGatewayManagementApiClient = clientFactory.provide(connectionInformation)) {
            final String connectionId = connectionInformation.connectionId;
            final DeleteConnectionRequest request = DeleteConnectionRequest.builder()
                    .connectionId(connectionId)
                    .build();
            apiGatewayManagementApiClient.deleteConnection(request);
        }
    }

    @Override
    public WebsocketSenderId senderId() {
        return AWS_WEBSOCKET_SENDER;
    }
}
