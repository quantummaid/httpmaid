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
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.Map;

import static de.quantummaid.httpmaid.util.Validators.validateNotNull;
import static de.quantummaid.httpmaid.websockets.WebsocketMetaDataKeys.*;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class RawWebsocketDisconnect implements RawRequest {
    private final ConnectionInformation connectionInformation;
    private final Map<MetaDataKey<?>, Object> additionalMetaData;

    public static RawWebsocketDisconnect rawWebsocketDisconnect(final ConnectionInformation connectionInformation) {
        return new RawWebsocketDisconnect(connectionInformation, Map.of());
    }

    public static RawWebsocketDisconnect rawWebsocketDisconnect(final ConnectionInformation connectionInformation,
                                                                final Map<MetaDataKey<?>, Object> additionalMetaData) {
        validateNotNull(connectionInformation, "connectionInformation");
        validateNotNull(additionalMetaData, "additionalMetaData");
        return new RawWebsocketDisconnect(connectionInformation, additionalMetaData);
    }

    @Override
    public void enter(final MetaData metaData) {
        metaData.set(WEBSOCKET_CONNECTION_INFORMATION, connectionInformation);
        metaData.set(REQUEST_TYPE, WEBSOCKET_DISCONNECT);
        additionalMetaData.forEach(metaData::setUnchecked);
    }
}
