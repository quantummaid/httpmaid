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

package de.quantummaid.httpmaid.awslambda.sender;

import de.quantummaid.httpmaid.awslambda.AwsWebsocketConnectionInformation;
import de.quantummaid.httpmaid.awslambda.sender.apigateway.AbstractGatewayClient;
import de.quantummaid.httpmaid.awslambda.sender.apigateway.ApiGatewayClientFactory;
import de.quantummaid.httpmaid.awslambda.sender.apigateway.GatewayOperation;
import de.quantummaid.httpmaid.websockets.sender.WebsocketSender;
import de.quantummaid.httpmaid.websockets.sender.WebsocketSenderId;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import static de.quantummaid.httpmaid.awslambda.sender.ClientCache.clientCache;
import static de.quantummaid.httpmaid.websockets.sender.WebsocketSenderId.websocketSenderId;
import static java.util.stream.Collectors.toMap;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class AwsWebsocketSender implements WebsocketSender<AwsWebsocketConnectionInformation>, AutoCloseable {
    public static final WebsocketSenderId AWS_WEBSOCKET_SENDER = websocketSenderId("AWS_WEBSOCKET_SENDER");

    private final ClientCache clientCache;

    public static AwsWebsocketSender awsWebsocketSender(final ApiGatewayClientFactory clientFactory) {
        final ClientCache clientCache = clientCache(clientFactory);
        return new AwsWebsocketSender(clientCache);
    }

    @Override
    public void send(final String message,
                     final List<AwsWebsocketConnectionInformation> connectionInformations,
                     final BiConsumer<AwsWebsocketConnectionInformation, Throwable> onException) {
        runOperation(connectionInformations, onException, (client, connectionId) ->
                client.sendMessage(connectionId, message));
    }

    @Override
    public void disconnect(final List<AwsWebsocketConnectionInformation> connectionInformations,
                           final BiConsumer<AwsWebsocketConnectionInformation, Throwable> onException) {
        runOperation(connectionInformations, onException, AbstractGatewayClient::disconnect);
    }

    private void runOperation(final List<AwsWebsocketConnectionInformation> connectionInformations,
                              final BiConsumer<AwsWebsocketConnectionInformation, Throwable> onException,
                              final BiFunction<AbstractGatewayClient, String, GatewayOperation> operation) {
        final Map<AwsWebsocketConnectionInformation, GatewayOperation> operations = connectionInformations.stream()
                .collect(toMap(connectionInformation -> connectionInformation, connectionInformation -> {
                    final String endpointUrl = connectionInformation.toEndpointUrl();
                    final AbstractGatewayClient client = clientCache.get(endpointUrl);
                    final String connectionId = connectionInformation.connectionId;
                    return operation.apply(client, connectionId);
                }));
        operations.forEach((connectionInformation, gatewayOperation) ->
                gatewayOperation.awaitResult(throwable -> onException.accept(connectionInformation, throwable)));
    }

    @Override
    public WebsocketSenderId senderId() {
        return AWS_WEBSOCKET_SENDER;
    }

    @Override
    public void close() {
        clientCache.close();
    }
}
