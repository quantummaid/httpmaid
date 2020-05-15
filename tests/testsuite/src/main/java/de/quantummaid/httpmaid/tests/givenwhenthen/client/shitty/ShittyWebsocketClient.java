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

package de.quantummaid.httpmaid.tests.givenwhenthen.client.shitty;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.glassfish.tyrus.client.ClientManager;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.lang.Thread.currentThread;

@ToString
@EqualsAndHashCode(callSuper = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShittyWebsocketClient extends Endpoint implements MessageHandler.Whole<String> {
    private final Consumer<String> responseHandler;
    private final CountDownLatch connectLatch = new CountDownLatch(1);
    private Session session;

    public static ShittyWebsocketClient openWebsocket(final String uri,
                                                      final Consumer<String> responseHandler,
                                                      final Map<String, List<String>> headers,
                                                      final Map<String, String> queryParameters) {
        final String queryParametersTail;
        if (queryParameters.isEmpty()) {
            queryParametersTail = "";
        } else {
            queryParametersTail = queryParameters.entrySet().stream()
                    .map(entry -> String.format("%s=%s", entry.getKey(), entry.getValue()))
                    .collect(Collectors.joining("&", "?", ""));
        }
        final String fullUri = uri + queryParametersTail;
        try {
            final ClientEndpointConfig.Builder configBuilder = ClientEndpointConfig.Builder.create();
            configBuilder.configurator(new ClientEndpointConfig.Configurator() {
                @Override
                public void beforeRequest(final Map<String, List<String>> currentHeaders) {
                    headers.forEach(currentHeaders::put);
                }
            });
            final ShittyWebsocketClient shittyWebsocketClient = new ShittyWebsocketClient(responseHandler);
            final ClientEndpointConfig clientEndpointConfig = configBuilder.build();
            final URI uriObject = new URI(fullUri);
            final ClientManager clientManager = ClientManager.createClient();
            clientManager.connectToServer(shittyWebsocketClient, clientEndpointConfig, uriObject);
            final boolean success = shittyWebsocketClient.connectLatch.await(1, TimeUnit.MINUTES);
            if (!success) {
                throw new RuntimeException(format("Timeout during connect to %s", fullUri));
            }
            return shittyWebsocketClient;
        } catch (final URISyntaxException | IOException | DeploymentException e) {
            throw new RuntimeException(format("Exception during connect to %s", fullUri), e);
        } catch (final InterruptedException e) {
            currentThread().interrupt();
            throw new RuntimeException(format("Interrupted during wait for connect to %s", fullUri));
        }
    }

    @Override
    public synchronized void onOpen(final Session session, final EndpointConfig config) {
        this.session = session;
        session.addMessageHandler(this);
        connectLatch.countDown();
    }

    @Override
    public void onMessage(final String message) {
        responseHandler.accept(message);
    }

    public synchronized void send(final String message) {
        try {
            session.getBasicRemote().sendText(message);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }
}
