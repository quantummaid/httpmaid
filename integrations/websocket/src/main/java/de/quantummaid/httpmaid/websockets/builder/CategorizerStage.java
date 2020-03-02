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

package de.quantummaid.httpmaid.websockets.builder;

import de.quantummaid.httpmaid.chains.MetaData;
import de.quantummaid.httpmaid.chains.MetaDataKey;
import de.quantummaid.httpmaid.websockets.WebSocketTag;
import de.quantummaid.httpmaid.websockets.WebSocketsConfigurator;

import java.util.function.Function;

import static de.quantummaid.httpmaid.websockets.WebSocketTag.webSocketTag;
import static de.quantummaid.httpmaid.websockets.WebsocketChainKeys.WEBSOCKET_TAG;

public interface CategorizerStage {

    default <T> WebSocketsConfigurator saving(final MetaDataKey<T> key) {
        return initializingMetaDataForIncomingMessagesWith(key, metaData -> metaData.get(key));
    }

    default WebSocketsConfigurator taggedBy(final String tag) {
        final WebSocketTag webSocketTag = webSocketTag(tag);
        return initializingMetaDataForIncomingMessagesWith(WEBSOCKET_TAG, metaData -> webSocketTag);
    }

    <T> WebSocketsConfigurator initializingMetaDataForIncomingMessagesWith(
            MetaDataKey<T> key, Function<MetaData, T> valueProvider);
}
