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

package de.quantummaid.httpmaid.websockets.disconnect;

import de.quantummaid.httpmaid.websockets.criteria.WebsocketCriteria;
import de.quantummaid.httpmaid.websockets.registry.ConnectionInformation;
import de.quantummaid.httpmaid.websockets.registry.WebsocketRegistry;
import de.quantummaid.httpmaid.websockets.registry.WebsocketRegistryEntry;
import de.quantummaid.httpmaid.websockets.sender.WebsocketSender;
import de.quantummaid.httpmaid.websockets.sender.WebsocketSenderId;
import de.quantummaid.httpmaid.websockets.sender.WebsocketSenders;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static de.quantummaid.httpmaid.util.Validators.validateNotNull;
import static de.quantummaid.httpmaid.websockets.criteria.WebsocketCriteria.websocketCriteria;
import static java.util.stream.Collectors.groupingBy;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class Disconnector {
    private final WebsocketRegistry websocketRegistry;
    private final WebsocketSenders websocketSenders;

    public static Disconnector disconnector(final WebsocketRegistry websocketRegistry,
                                            final WebsocketSenders websocketSenders) {
        return new Disconnector(websocketRegistry, websocketSenders);
    }

    public void disconnectAll() {
        disconnectAllThat(websocketCriteria());
    }

    public void disconnectAllThat(final WebsocketCriteria criteria) {
        validateNotNull(criteria, "criteria");
        final List<WebsocketRegistryEntry> connections = websocketRegistry.connections(criteria);
        final Map<WebsocketSenderId, List<WebsocketRegistryEntry>> bySenderId = connections.stream()
                .collect(groupingBy(WebsocketRegistryEntry::getSenderId));
        bySenderId.forEach((websocketSenderId, websocketRegistryEntries) -> {
            final List<ConnectionInformation> collectionInformations = websocketRegistryEntries.stream()
                    .map(WebsocketRegistryEntry::connectionInformation)
                    .collect(Collectors.toList());
            collectionInformations.forEach(websocketRegistry::removeConnection);
            final WebsocketSender<ConnectionInformation> sender = websocketSenders.senderById(websocketSenderId);
            sender.disconnect(collectionInformations, (connectionInformation, throwable) ->
                    log.info("Exception when disconnecting websocket {}.", connectionInformation, throwable));
        });
    }
}
