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
import de.quantummaid.httpmaid.chains.MetaDataKey;
import de.quantummaid.httpmaid.websockets.registry.WebsocketRegistry;
import de.quantummaid.httpmaid.websockets.sender.WebsocketSenders;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.*;
import java.util.stream.Collectors;

import static de.quantummaid.httpmaid.chains.MetaDataKey.metaDataKey;
import static de.quantummaid.httpmaid.websockets.WebsocketMetaDataKeys.WEBSOCKET_REGISTRY;
import static de.quantummaid.httpmaid.websockets.broadcast.SerializingSender.serializingSender;
import static de.quantummaid.httpmaid.websockets.sender.WebsocketSenders.WEBSOCKET_SENDERS;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@SuppressWarnings("java:S1845")
public final class Broadcasters {
    public static final MetaDataKey<Broadcasters> BROADCASTERS = metaDataKey("BROADCASTERS");

    private final Map<Class<?>, BroadcasterFactory<?, Object>> factories = new HashMap<>();
    private final List<Class<?>> messageTypes = new ArrayList<>();

    public static Broadcasters broadcasters() {
        return new Broadcasters();
    }

    @SuppressWarnings("unchecked")
    public <T, U> void addBroadcaster(final Class<T> type,
                                      final Class<U> messageType,
                                      final BroadcasterFactory<T, U> factory) {
        factories.put(type, (BroadcasterFactory<?, Object>) factory);
        messageTypes.add(messageType);
    }

    public Collection<Class<?>> injectionTypes() {
        return factories.keySet();
    }

    public List<Object> instantiateAll(final MetaData metaData) {
        final WebsocketSenders websocketSenders = metaData.get(WEBSOCKET_SENDERS);
        final WebsocketRegistry websocketRegistry = metaData.get(WEBSOCKET_REGISTRY);
        final SerializingSender<Object> serializingSender = serializingSender(websocketRegistry, websocketSenders);
        return factories.values().stream()
                .map(broadcasterFactory -> broadcasterFactory.createBroadcaster(serializingSender))
                .collect(Collectors.toList());
    }
}
