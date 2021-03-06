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

import de.quantummaid.httpmaid.chains.MetaDataKey;
import de.quantummaid.httpmaid.runtimeconfiguration.RuntimeConfigurationValue;
import de.quantummaid.httpmaid.websockets.registry.ConnectionInformation;
import de.quantummaid.httpmaid.websockets.registry.WebsocketRegistry;
import de.quantummaid.httpmaid.websockets.registry.WebsocketRegistryEntry;

import java.util.Map;

import static de.quantummaid.httpmaid.chains.MetaDataKey.metaDataKey;

public final class WebsocketMetaDataKeys {
    public static final String WEBSOCKET_AUTHORIZATION = "WEBSOCKET_AUTHORIZATION";
    public static final String WEBSOCKET_CONNECT = "WEBSOCKET_CONNECT";
    public static final String WEBSOCKET_MESSAGE = "WEBSOCKET_MESSAGE";
    public static final String WEBSOCKET_DISCONNECT = "WEBSOCKET_DISCONNECT";

    public static final MetaDataKey<RuntimeConfigurationValue<WebsocketRegistry>> WEBSOCKET_REGISTRY = metaDataKey("WEBSOCKET_REGISTRY");

    public static final MetaDataKey<Map<String, Object>> ADDITIONAL_WEBSOCKET_DATA = metaDataKey("ADDITIONAL_WEBSOCKET_DATA");

    public static final MetaDataKey<String> REQUEST_TYPE = metaDataKey("REQUEST_TYPE");
    public static final MetaDataKey<ConnectionInformation> WEBSOCKET_CONNECTION_INFORMATION = metaDataKey("WEBSOCKET_CONNECTION_INFORMATION");
    public static final MetaDataKey<String> WEBSOCKET_ROUTE = metaDataKey("WEBSOCKET_ROUTE");
    public static final MetaDataKey<WebsocketRegistryEntry> WEBSOCKET_REGISTRY_ENTRY = metaDataKey("WEBSOCKET_REGISTRY_ENTRY");

    private WebsocketMetaDataKeys() {
    }
}
