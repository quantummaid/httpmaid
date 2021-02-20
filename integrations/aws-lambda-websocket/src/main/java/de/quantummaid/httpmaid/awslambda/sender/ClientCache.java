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

import de.quantummaid.httpmaid.awslambda.sender.apigateway.AbstractGatewayClient;
import de.quantummaid.httpmaid.awslambda.sender.apigateway.ApiGatewayClientFactory;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ClientCache implements AutoCloseable {
    private final Map<String, AbstractGatewayClient> clientsByEndpointUrl;
    private final ApiGatewayClientFactory clientFactory;

    public static ClientCache clientCache(final ApiGatewayClientFactory clientFactory) {
        return new ClientCache(new ConcurrentHashMap<>(), clientFactory);
    }

    public AbstractGatewayClient get(final String endpointUrl) {
        return clientsByEndpointUrl.computeIfAbsent(endpointUrl, clientFactory::provide);
    }

    @Override
    public void close() {
        clientFactory.close();
        for (final AbstractGatewayClient client : clientsByEndpointUrl.values()) {
            client.close();
        }
    }
}
