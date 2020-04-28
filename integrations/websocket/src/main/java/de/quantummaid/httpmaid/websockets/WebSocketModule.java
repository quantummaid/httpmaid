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

package de.quantummaid.httpmaid.websockets;

import de.quantummaid.httpmaid.websockets.processors.*;
import de.quantummaid.httpmaid.websockets.registry.WebSocketRegistry;
import de.quantummaid.httpmaid.MetricsProvider;
import de.quantummaid.httpmaid.chains.ChainExtender;
import de.quantummaid.httpmaid.chains.ChainModule;
import de.quantummaid.httpmaid.chains.DependencyRegistry;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.LinkedList;
import java.util.List;

import static de.quantummaid.httpmaid.HttpMaidChains.*;
import static de.quantummaid.httpmaid.chains.rules.Consume.consume;
import static de.quantummaid.httpmaid.chains.rules.Jump.jumpTo;
import static de.quantummaid.httpmaid.util.Validators.validateNotNull;
import static de.quantummaid.httpmaid.websockets.WebSocketMetrics.NUMBER_OF_ACTIVE_WEB_SOCKETS;
import static de.quantummaid.httpmaid.websockets.WebsocketChainKeys.*;
import static de.quantummaid.httpmaid.websockets.WebsocketChains.*;
import static de.quantummaid.httpmaid.websockets.processors.CreateWebSocketProcessor.createWebSocketProcessor;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class WebSocketModule implements ChainModule {
    private final List<WebSocketMapping> webSocketMappings = new LinkedList<>();

    public static WebSocketModule webSocketModule() {
        return new WebSocketModule();
    }

    public void addWebSocketMapping(final WebSocketMapping webSocketMapping) {
        validateNotNull(webSocketMapping, "webSocketMapping");
        webSocketMappings.add(webSocketMapping);
    }

    @Override
    public void configure(final DependencyRegistry dependencyRegistry) {
        final MetricsProvider<Integer> metricsProvider =
                dependencyRegistry.createMetricsProvider(NUMBER_OF_ACTIVE_WEB_SOCKETS, 0);
        dependencyRegistry.setMetaDatum(WEBSOCKET_REGISTRY, WebSocketRegistry.webSocketRegistry(metricsProvider));
    }

    @Override
    public void register(final ChainExtender extender) {
        final WebSocketRegistry registry = extender.getMetaDatum(WEBSOCKET_REGISTRY);
        createSkeleton(extender);
        extender.appendProcessor(INIT, WebSocketInitializationProcessor.webSocketInitializationProcessor(registry));
        extender.appendProcessor(WEBSOCKET_ESTABLISHMENT, createWebSocketProcessor(registry));
        extender.appendProcessor(DETERMINE_WEBSOCKET_TYPE, DetermineWebSocketTypeProcessor.determineWebSocketTypeProcessor(webSocketMappings));
        extender.appendProcessor(WEBSOCKET_OPEN, ActivateWebSocketProcessor.activateWebSocketProcessor(registry));
        extender.appendProcessor(WEBSOCKET_CLOSED, RemoveWebSocketFromRegistryProcessor.removeWebSocketFromRegistryProcessor(registry));
    }

    private static void createSkeleton(final ChainExtender extender) {
        extender.createChain(WEBSOCKET_ESTABLISHMENT, consume(), jumpTo(EXCEPTION_OCCURRED));
        extender.createChain(DETERMINE_WEBSOCKET_TYPE, jumpTo(WEBSOCKET_ESTABLISHMENT), jumpTo(EXCEPTION_OCCURRED));
        extender.routeIfSet(PROCESS_HEADERS, jumpTo(DETERMINE_WEBSOCKET_TYPE), WEBSOCKET_ID);
        extender.createChain(WEBSOCKET_OPEN, consume(), jumpTo(EXCEPTION_OCCURRED));
        extender.createChain(WEBSOCKET_MESSAGE, jumpTo(PROCESS_BODY), jumpTo(EXCEPTION_OCCURRED));
        extender.createChain(SEND_TO_WEBSOCKETS, consume(), jumpTo(EXCEPTION_OCCURRED));
        extender.createChain(WEBSOCKET_CLOSED, consume(), jumpTo(EXCEPTION_OCCURRED));
        extender.createChain(WEBSOCKET_CLOSE, consume(), jumpTo(EXCEPTION_OCCURRED));

        extender.routeIfFlagIsSet(INIT, jumpTo(WEBSOCKET_MESSAGE), IS_WEBSOCKET_MESSAGE);
        extender.routeIfFlagIsSet(POST_PROCESS, jumpTo(SEND_TO_WEBSOCKETS), IS_WEBSOCKET_MESSAGE);
    }
}
