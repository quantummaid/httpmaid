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
import de.quantummaid.httpmaid.websockets.registry.ConnectionInformation;
import de.quantummaid.httpmaid.websockets.registry.WebsocketRegistry;
import de.quantummaid.httpmaid.websockets.registry.WebsocketRegistryEntry;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import static de.quantummaid.httpmaid.websockets.WebsocketMetaDataKeys.*;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class RestoreWebsocketContextInformationProcessor implements Processor {

    public static Processor restoreWebsocketContextInformationProcessor() {
        return new RestoreWebsocketContextInformationProcessor();
    }

    @Override
    public void apply(final MetaData metaData) {
        final boolean isWebsocketMessage = metaData.getOptional(REQUEST_TYPE)
                .map(WEBSOCKET_MESSAGE::equals)
                .orElse(false);
        if (isWebsocketMessage) {
            final WebsocketRegistry websocketRegistry = metaData.get(WEBSOCKET_REGISTRY);
            final ConnectionInformation connectionInformation = metaData.get(WEBSOCKET_CONNECTION_INFORMATION);
            if (metaData.getOptional(RESTORATION_FROM_REGISTRY_NEEDED).orElse(true)) {
                final WebsocketRegistryEntry entry = websocketRegistry.byConnectionInformation(connectionInformation);
                entry.restoreToMetaData(metaData);
            }
        }
    }
}
