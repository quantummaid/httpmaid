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

package de.quantummaid.httpmaid.websockets.endpoint;

import de.quantummaid.httpmaid.chains.MetaData;
import de.quantummaid.httpmaid.chains.MetaDataKey;
import de.quantummaid.httpmaid.endpoint.RawRequest;
import de.quantummaid.httpmaid.websockets.registry.ConnectionInformation;
import de.quantummaid.httpmaid.websockets.registry.WebsocketRegistryEntry;
import de.quantummaid.httpmaid.websockets.sender.WebsocketSenderId;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Map;

import static de.quantummaid.httpmaid.util.Validators.validateNotNull;
import static de.quantummaid.httpmaid.websockets.WebsocketMetaDataKeys.*;
import static de.quantummaid.httpmaid.websockets.sender.WebsocketSenderId.WEBSOCKET_SENDER_ID;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class RawWebsocketConnect implements RawRequest {
    private final ConnectionInformation connectionInformation;
    private final WebsocketSenderId websocketSenderId;
    private final WebsocketRegistryEntry registryEntry;
    private final Map<MetaDataKey<?>, Object> additionalMetaData;

    public static RawWebsocketConnectBuilder rawWebsocketConnectBuilder() {
        return RawWebsocketConnectBuilder.rawWebsocketConnectBuilder();
    }

    public static RawWebsocketConnect rawWebsocketConnect(final ConnectionInformation connectionInformation,
                                                          final WebsocketSenderId websocketSenderId,
                                                          final WebsocketRegistryEntry registryEntry) {
        return rawWebsocketConnect(connectionInformation, websocketSenderId, registryEntry, Map.of());
    }

    public static RawWebsocketConnect rawWebsocketConnect(final ConnectionInformation connectionInformation,
                                                          final WebsocketSenderId websocketSenderId,
                                                          final WebsocketRegistryEntry registryEntry,
                                                          final Map<MetaDataKey<?>, Object> additionalMetaData) {
        validateNotNull(connectionInformation, "connectionInformation");
        validateNotNull(websocketSenderId, "websocketSenderId");
        validateNotNull(registryEntry, "registryEntry");
        validateNotNull(additionalMetaData, "additionalMetaData");
        return new RawWebsocketConnect(connectionInformation,
                websocketSenderId,
                registryEntry,
                additionalMetaData);
    }

    @Override
    public void enter(final MetaData metaData) {
        metaData.set(WEBSOCKET_CONNECTION_INFORMATION, connectionInformation);
        metaData.set(WEBSOCKET_SENDER_ID, websocketSenderId);
        metaData.set(WEBSOCKET_REGISTRY_ENTRY, registryEntry);
        metaData.set(REQUEST_TYPE, WEBSOCKET_CONNECT);
        additionalMetaData.forEach(metaData::setUnchecked);
    }
}
