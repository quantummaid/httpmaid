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

import de.quantummaid.httpmaid.websockets.registry.WebSocketId;
import de.quantummaid.httpmaid.websockets.registry.WebSocketRegistry;
import de.quantummaid.httpmaid.chains.MetaDataKey;

import java.util.List;

import static de.quantummaid.httpmaid.chains.MetaDataKey.metaDataKey;

public final class WebsocketChainKeys {
    public static final MetaDataKey<Boolean> IS_WEBSOCKET_MESSAGE = metaDataKey("IS_WEBSOCKET_MESSAGE");
    public static final MetaDataKey<WebSocketId> WEBSOCKET_ID = metaDataKey("WEBSOCKET_ID");
    public static final MetaDataKey<WebSocketMapping> WEBSOCKET_MAPPING = metaDataKey("WEBSOCKET_MAPPING");
    public static final MetaDataKey<WebSocketDelegate> WEBSOCKET_DELEGATE = metaDataKey("WEBSOCKET_DELEGATE");
    public static final MetaDataKey<Boolean> WEBSOCKET_ACCEPTED = metaDataKey("WEBSOCKET_ACCEPTED");

    public static final MetaDataKey<WebSocketRegistry> WEBSOCKET_REGISTRY = metaDataKey("WEBSOCKET_REGISTRY");
    public static final MetaDataKey<List<WebSocket>> RECIPIENT_WEBSOCKETS = metaDataKey("RECIPIENT_WEBSOCKETS");

    public static final MetaDataKey<WebSocketTag> WEBSOCKET_TAG = metaDataKey("WEBSOCKET_TAG");

    private WebsocketChainKeys() {
    }
}
