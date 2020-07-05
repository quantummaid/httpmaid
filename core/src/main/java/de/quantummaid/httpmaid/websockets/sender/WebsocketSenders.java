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

package de.quantummaid.httpmaid.websockets.sender;

import de.quantummaid.httpmaid.chains.MetaDataKey;
import de.quantummaid.httpmaid.websockets.registry.ConnectionInformation;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static de.quantummaid.httpmaid.chains.MetaDataKey.metaDataKey;
import static de.quantummaid.httpmaid.websockets.sender.NonSerializableWebsocketSender.NON_SERIALIZABLE_WEBSOCKET_SENDER;
import static de.quantummaid.httpmaid.websockets.sender.NonSerializableWebsocketSender.nonSerializableWebsocketSender;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class WebsocketSenders {
    public static final MetaDataKey<WebsocketSenders> WEBSOCKET_SENDERS = metaDataKey("WEBSOCKET_SENDERS");

    private final Map<WebsocketSenderId, WebsocketSender<ConnectionInformation>> senders;

    public static WebsocketSenders websocketSenders() {
        final WebsocketSenders websocketSenders = new WebsocketSenders(new ConcurrentHashMap<>());
        websocketSenders.addWebsocketSender(NON_SERIALIZABLE_WEBSOCKET_SENDER, nonSerializableWebsocketSender());
        return websocketSenders;
    }

    @SuppressWarnings("unchecked")
    public void addWebsocketSender(final WebsocketSenderId websocketSenderId,
                                   final WebsocketSender<?> websocketSender) {
        senders.put(websocketSenderId, (WebsocketSender<ConnectionInformation>) websocketSender);
    }

    public WebsocketSender<ConnectionInformation> senderById(final WebsocketSenderId id) {
        return senders.get(id);
    }
}
