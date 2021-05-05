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
import de.quantummaid.httpmaid.runtimeconfiguration.RuntimeConfigurationValue;
import de.quantummaid.httpmaid.websockets.registry.WebsocketRegistry;
import de.quantummaid.httpmaid.websockets.sender.WebsocketSenders;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import static de.quantummaid.httpmaid.util.Validators.validateNotNull;
import static de.quantummaid.httpmaid.websockets.WebsocketMetaDataKeys.WEBSOCKET_REGISTRY;
import static de.quantummaid.httpmaid.websockets.sender.WebsocketSenders.WEBSOCKET_SENDERS;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class AddWebsocketsMetaDataProcessor implements Processor {
    private final WebsocketSenders websocketSenders;
    private final RuntimeConfigurationValue<WebsocketRegistry> registry;

    public static Processor addWebsocketRegistryProcessor(final WebsocketSenders websocketSenders,
                                                          final RuntimeConfigurationValue<WebsocketRegistry> registry) {
        validateNotNull(websocketSenders, "websocketSenders");
        validateNotNull(registry, "registry");
        return new AddWebsocketsMetaDataProcessor(websocketSenders, registry);
    }

    @Override
    public void apply(final MetaData metaData) {
        metaData.set(WEBSOCKET_SENDERS, websocketSenders);
        metaData.set(WEBSOCKET_REGISTRY, registry);
    }
}
