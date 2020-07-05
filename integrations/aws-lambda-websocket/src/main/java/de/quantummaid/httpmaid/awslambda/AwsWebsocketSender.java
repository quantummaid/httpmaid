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
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.apigatewaymanagementapi.ApiGatewayManagementApiAsyncClient;
import software.amazon.awssdk.services.apigatewaymanagementapi.model.DeleteConnectionRequest;
import software.amazon.awssdk.services.apigatewaymanagementapi.model.DeleteConnectionResponse;
import software.amazon.awssdk.services.apigatewaymanagementapi.model.PostToConnectionRequest;
import software.amazon.awssdk.services.apigatewaymanagementapi.model.PostToConnectionResponse;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

import static de.quantummaid.httpmaid.awslambda.ConnectionFuture.connectionFuture;
import static de.quantummaid.httpmaid.websockets.sender.WebsocketSenderId.websocketSenderId;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class AwsWebsocketSender implements WebsocketSender<AwsWebsocketConnectionInformation> {
    public static final WebsocketSenderId AWS_WEBSOCKET_SENDER = websocketSenderId("AWS_WEBSOCKET_SENDER");

    private final ApiGatewayClientFactory clientFactory;

    public static AwsWebsocketSender awsWebsocketSender(final ApiGatewayClientFactory clientFactory) {
        return new AwsWebsocketSender(clientFactory);
    }

    @Override
    public void send(final String message,
                     final List<AwsWebsocketConnectionInformation> connectionInformations,
                     final BiConsumer<AwsWebsocketConnectionInformation, Throwable> onException) {
        final Map<String, List<AwsWebsocketConnectionInformation>> groupedByEndpointUrl = connectionInformations.stream()
                .collect(groupingBy(AwsWebsocketConnectionInformation::toEndpointUrl));
        groupedByEndpointUrl.forEach((endpointUrl, awsWebsocketConnectionInformations) -> {
            try (ApiGatewayManagementApiAsyncClient client = clientFactory.provide(endpointUrl)) {
                final List<ConnectionFuture> futures = awsWebsocketConnectionInformations.stream()
                        .map(connectionInformation -> {
                            final String connectionId = connectionInformation.connectionId;
                            final PostToConnectionRequest request = PostToConnectionRequest.builder()
                                    .connectionId(connectionId)
                                    .data(SdkBytes.fromUtf8String(message))
                                    .build();
                            final CompletableFuture<PostToConnectionResponse> future = client.postToConnection(request);
                            return connectionFuture(connectionInformation, future);
                        })
                        .collect(toList());
                waitForAllFutures(futures, onException);
            }
        });
    }

    @Override
    public void disconnect(final List<AwsWebsocketConnectionInformation> connectionInformations,
                           final BiConsumer<AwsWebsocketConnectionInformation, Throwable> onException) {
        final Map<String, List<AwsWebsocketConnectionInformation>> groupedByEndpointUrl = connectionInformations.stream()
                .collect(groupingBy(AwsWebsocketConnectionInformation::toEndpointUrl));
        groupedByEndpointUrl.forEach((endpointUrl, awsWebsocketConnectionInformations) -> {
            try (ApiGatewayManagementApiAsyncClient client = clientFactory.provide(endpointUrl)) {
                final List<ConnectionFuture> futures = awsWebsocketConnectionInformations.stream()
                        .map(connectionInformation -> {
                            final String connectionId = connectionInformation.connectionId;
                            final DeleteConnectionRequest request = DeleteConnectionRequest.builder()
                                    .connectionId(connectionId)
                                    .build();
                            final CompletableFuture<DeleteConnectionResponse> future = client.deleteConnection(request);
                            return connectionFuture(connectionInformation, future);
                        })
                        .collect(toList());
                waitForAllFutures(futures, onException);
            }
        });
    }

    @Override
    public WebsocketSenderId senderId() {
        return AWS_WEBSOCKET_SENDER;
    }

    private static void waitForAllFutures(final List<ConnectionFuture> futures,
                                          final BiConsumer<AwsWebsocketConnectionInformation, Throwable> onException) {
        while (true) {
            final boolean allDone = futures.stream().allMatch(ConnectionFuture::isDone);
            if (allDone) {
                break;
            }
        }
        futures.forEach(future -> future.check(onException));
    }
}
