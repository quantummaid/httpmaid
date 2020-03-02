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

package de.quantummaid.httpmaid.websockets.handler;

import de.quantummaid.httpmaid.generator.GenerationCondition;
import de.quantummaid.httpmaid.websockets.WebSocketTag;

import static de.quantummaid.httpmaid.websockets.WebSocketTag.webSocketTag;
import static de.quantummaid.httpmaid.websockets.WebsocketChainKeys.WEBSOCKET_TAG;

public final class WebSocketCondition {

    private WebSocketCondition() {
    }

    public static GenerationCondition webSocketMessageIsTaggedWith(final String tag) {
        final WebSocketTag expectedTag = webSocketTag(tag);
        return metaData -> metaData.getOptional(WEBSOCKET_TAG)
                .map(expectedTag::equals)
                .orElse(false);
    }
}
