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

package de.quantummaid.httpmaid.websockets.registry;

import de.quantummaid.httpmaid.websockets.criteria.WebsocketCriteria;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class InMemoryRegistry implements WebsocketRegistry {
    private final List<WebsocketRegistryEntry> entries;

    public static InMemoryRegistry inMemoryRegistry() {
        return new InMemoryRegistry(new ArrayList<>());
    }

    @Override
    public synchronized List<WebsocketRegistryEntry> connections(final WebsocketCriteria criteria) {
        return entries.stream()
                .filter(criteria::filter)
                .collect(toList());
    }

    @Override
    public synchronized WebsocketRegistryEntry byConnectionInformation(final ConnectionInformation connectionInformation) {
        return entries.stream()
                .filter(entry -> entry.connectionInformation().equals(connectionInformation))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(format("No websocket registered by '%s'", connectionInformation)));
    }

    @Override
    public synchronized void addConnection(final WebsocketRegistryEntry entry) {
        entries.add(entry);
    }

    @Override
    public synchronized void removeConnection(final ConnectionInformation connectionInformation) {
        final List<WebsocketRegistryEntry> entriesToRemove = entries.stream()
                .filter(entry -> entry.connectionInformation().equals(connectionInformation))
                .collect(toList());
        entries.removeAll(entriesToRemove);
    }

    @Override
    public long countConnections() {
        return entries.size();
    }
}
