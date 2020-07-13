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
import de.quantummaid.httpmaid.marshalling.Marshaller;
import de.quantummaid.httpmaid.marshalling.Marshallers;
import de.quantummaid.httpmaid.serialization.Serializer;
import de.quantummaid.httpmaid.websockets.disconnect.Disconnector;
import de.quantummaid.httpmaid.websockets.disconnect.DisconnectorFactory;
import de.quantummaid.httpmaid.websockets.registry.WebsocketRegistry;
import de.quantummaid.httpmaid.websockets.sender.WebsocketSenders;
import de.quantummaid.reflectmaid.ResolvedType;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static de.quantummaid.httpmaid.chains.MetaDataKey.metaDataKey;
import static de.quantummaid.httpmaid.marshalling.Marshallers.MARSHALLERS;
import static de.quantummaid.httpmaid.serialization.Serializer.SERIALIZER;
import static de.quantummaid.httpmaid.websockets.WebsocketMetaDataKeys.WEBSOCKET_REGISTRY;
import static de.quantummaid.httpmaid.websockets.broadcast.RegisteredBroadcasterFactory.registeredBroadcasterFactory;
import static de.quantummaid.httpmaid.websockets.broadcast.SerializingSender.serializingSender;
import static de.quantummaid.httpmaid.websockets.disconnect.Disconnector.disconnector;
import static de.quantummaid.httpmaid.websockets.sender.WebsocketSenders.WEBSOCKET_SENDERS;
import static java.util.stream.Collectors.toList;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@SuppressWarnings("java:S1845")
public final class Broadcasters {
    public static final MetaDataKey<Broadcasters> BROADCASTERS = metaDataKey("BROADCASTERS");

    private final List<RegisteredBroadcasterFactory> broadcasterFactories = new ArrayList<>();
    private final Map<Class<?>, DisconnectorFactory<?>> disconnectorFactories = new LinkedHashMap<>();

    public static Broadcasters broadcasters() {
        return new Broadcasters();
    }

    public <T, U> void addBroadcaster(final Class<T> type,
                                      final Class<U> messageType,
                                      final BroadcasterFactory<T, U> factory) {
        final RegisteredBroadcasterFactory registeredFactory = registeredBroadcasterFactory(factory, type, messageType);
        broadcasterFactories.add(registeredFactory);
    }

    public <T> void addDisconnector(final Class<T> type,
                                    final DisconnectorFactory<T> factory) {
        disconnectorFactories.put(type, factory);
    }

    public List<Class<?>> injectionTypes() {
        final List<Class<?>> injectionTypes = new ArrayList<>();
        broadcasterFactories.stream()
                .map(RegisteredBroadcasterFactory::senderType)
                .map(ResolvedType::assignableType)
                .forEach(injectionTypes::add);
        injectionTypes.addAll(disconnectorFactories.keySet());
        return injectionTypes;
    }

    public List<Class<?>> messageTypes() {
        return broadcasterFactories.stream()
                .map(RegisteredBroadcasterFactory::messageType)
                .map(ResolvedType::assignableType)
                .collect(toList());
    }

    public List<Object> instantiateAll(final MetaData metaData) {
        final WebsocketSenders websocketSenders = metaData.get(WEBSOCKET_SENDERS);
        final WebsocketRegistry websocketRegistry = metaData.get(WEBSOCKET_REGISTRY);
        final Serializer serializer = metaData.get(SERIALIZER);
        final List<Object> instances = new ArrayList<>();
        broadcasterFactories.stream()
                .map(broadcasterFactory -> {
                    final Marshallers marshallers = metaData.get(MARSHALLERS);
                    final Marshaller marshaller = marshallers.determineResponseMarshaller(metaData);
                    final ResolvedType messageType = broadcasterFactory.messageType();
                    final SerializingSender<Object> serializingSender = serializingSender(
                            websocketRegistry,
                            websocketSenders,
                            messageType,
                            marshaller,
                            serializer
                    );
                    final BroadcasterFactory<?, Object> factory = broadcasterFactory.factory();
                    return factory.createBroadcaster(serializingSender);
                })
                .forEach(instances::add);
        final Disconnector disconnector = disconnector(websocketRegistry, websocketSenders);
        disconnectorFactories.values().stream()
                .map(disconnectorFactory -> disconnectorFactory.createDisconnector(disconnector))
                .forEach(instances::add);
        return instances;
    }
}
