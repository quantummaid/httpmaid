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

package de.quantummaid.httpmaid.websockets.broadcast;

import de.quantummaid.httpmaid.chains.MetaData;
import de.quantummaid.httpmaid.websockets.criteria.WebsocketCriteria;
import de.quantummaid.httpmaid.websockets.registry.ConnectionInformation;
import de.quantummaid.httpmaid.websockets.registry.WebsocketRegistry;
import de.quantummaid.httpmaid.websockets.registry.WebsocketRegistryEntry;
import de.quantummaid.httpmaid.websockets.sender.WebsocketSender;
import de.quantummaid.httpmaid.websockets.sender.WebsocketSenderId;
import de.quantummaid.httpmaid.websockets.sender.WebsocketSenders;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

import static de.quantummaid.httpmaid.util.Validators.validateNotNull;
import static de.quantummaid.httpmaid.websockets.criteria.WebsocketCriteria.websocketCriteria;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class NonSerializingSender {
    private final WebsocketRegistry websocketRegistry;
    private final WebsocketSenders websocketSenders;
    private final MetaData metaData;

    public static NonSerializingSender nonSerializingSender(final WebsocketRegistry websocketRegistry,
                                                            final WebsocketSenders websocketSenders,
                                                            final MetaData metaData) {
        return new NonSerializingSender(websocketRegistry, websocketSenders, metaData);
    }

    public void sendToAll(final String message) {
        sendTo(message, websocketCriteria());
    }

    public void sendTo(final String message, final WebsocketCriteria criteria) {
        validateNotNull(message, "message");
        validateNotNull(criteria, "criteria");
        final List<WebsocketRegistryEntry> connections = websocketRegistry.connections(criteria);
        final Map<WebsocketSenderId, List<WebsocketRegistryEntry>> bySenderId = connections.stream()
                .collect(groupingBy(WebsocketRegistryEntry::getSenderId));
        bySenderId.forEach((websocketSenderId, websocketRegistryEntries) -> {
            final List<ConnectionInformation> collectionInformations = websocketRegistryEntries.stream()
                    .map(WebsocketRegistryEntry::connectionInformation)
                    .collect(toList());
            final WebsocketSender<ConnectionInformation> sender = websocketSenders.senderById(websocketSenderId);
            sender.send(message, collectionInformations, (connectionInformation, throwable) -> {
                log.info("exception when sending to websocket {} - removing websocket; request metadata: {}", connectionInformation, metaData, throwable);
                websocketRegistry.removeConnection(connectionInformation);
            });
        });
    }
}
