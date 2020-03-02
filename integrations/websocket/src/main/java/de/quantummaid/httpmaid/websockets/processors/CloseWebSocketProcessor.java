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

package de.quantummaid.httpmaid.websockets.processors;

import de.quantummaid.httpmaid.chains.MetaData;
import de.quantummaid.httpmaid.chains.Processor;
import de.quantummaid.httpmaid.websockets.WebSocket;
import de.quantummaid.httpmaid.websockets.registry.WebSocketRegistry;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.List;

import static de.quantummaid.httpmaid.util.Validators.validateNotNull;
import static de.quantummaid.httpmaid.websockets.WebsocketChainKeys.WEBSOCKETS_TO_CLOSE;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class CloseWebSocketProcessor implements Processor {
    private final WebSocketRegistry registry;

    public static Processor closeWebSocketProcessor(final WebSocketRegistry registry) {
        validateNotNull(registry, "registry");
        return new CloseWebSocketProcessor(registry);
    }

    @Override
    public void apply(final MetaData metaData) {
        final List<WebSocket> webSockets = metaData.get(WEBSOCKETS_TO_CLOSE);
        webSockets.forEach(webSocket -> {
            registry.unregister(webSocket.id());
            webSocket.close();
        });
        /*
        final WebSocketId id = metaData.get(WEBSOCKET_ID);
        final WebSocket webSocket = registry.byId(id);
        registry.unregister(id);
        webSocket.close();
         */
    }
}
