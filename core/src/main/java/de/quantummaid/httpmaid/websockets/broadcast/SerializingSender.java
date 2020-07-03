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

import static de.quantummaid.httpmaid.util.Validators.validateNotNull;
import static de.quantummaid.httpmaid.websockets.criteria.WebsocketCriteria.websocketCriteria;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class SerializingSender<T> {
    private final WebsocketRegistry websocketRegistry;
    private final WebsocketSenders websocketSenders;

    public static <T> SerializingSender<T> serializingSender(final WebsocketRegistry websocketRegistry,
                                                             final WebsocketSenders websocketSenders) {
        return new SerializingSender<>(websocketRegistry, websocketSenders);
    }

    public void sendToAll(final T message) {
        sendTo(message, websocketCriteria());
    }

    public void sendTo(final T message, final WebsocketCriteria criteria) {
        validateNotNull(message, "message");
        validateNotNull(criteria, "criteria");
        final List<WebsocketRegistryEntry> connections = websocketRegistry.connections(criteria);
        connections.forEach(connection -> {
            final WebsocketSenderId websocketSenderId = connection.senderId();
            final WebsocketSender<Object> sender = websocketSenders.senderById(websocketSenderId);
            final ConnectionInformation connectionInformation = connection.connectionInformation();
            try {
                sender.send(connectionInformation, (String) message);
            } catch (final Exception e) {
                log.info("Exception when sending to websocket {}. Removing websocket.", connectionInformation, e);
                websocketRegistry.removeConnection(connectionInformation);
            }
        });
    }
}
