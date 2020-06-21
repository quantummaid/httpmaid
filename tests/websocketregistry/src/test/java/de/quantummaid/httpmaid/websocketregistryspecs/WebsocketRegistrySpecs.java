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

package de.quantummaid.httpmaid.websocketregistryspecs;

import de.quantummaid.httpmaid.websocketregistryspecs.testsupport.WebsocketRegistryDeployment;
import de.quantummaid.httpmaid.websockets.registry.ConnectionInformation;
import de.quantummaid.httpmaid.websockets.registry.WebsocketRegistry;
import de.quantummaid.httpmaid.websockets.registry.WebsocketRegistryEntry;
import org.junit.jupiter.api.Test;

import java.util.List;

import static de.quantummaid.httpmaid.http.Headers.headers;
import static de.quantummaid.httpmaid.http.QueryParameters.queryParameters;
import static de.quantummaid.httpmaid.http.headers.ContentType.json;
import static de.quantummaid.httpmaid.websockets.registry.WebsocketRegistryEntry.websocketRegistryEntry;
import static de.quantummaid.httpmaid.websockets.sender.WebsocketSenderId.websocketSenderId;
import static java.util.Collections.emptyList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public interface WebsocketRegistrySpecs {

    WebsocketRegistryDeployment websocketRegistry();

    ConnectionInformation connectionInformation();

    @Test
    default void individualConnectionCanBeQueried(final WebsocketRegistry websocketRegistry) {
        final ConnectionInformation connectionInformation = connectionInformation();
        final WebsocketRegistryEntry entry = websocketRegistryEntry(
                connectionInformation,
                websocketSenderId("foo"),
                headers(emptyList()),
                json(),
                queryParameters(emptyList())
        );
        websocketRegistry.addConnection(entry);
        final WebsocketRegistryEntry queriedEntry = websocketRegistry.byConnectionInformation(connectionInformation);
        assertThat(queriedEntry.getSenderId().asString(), is("foo"));
    }

    @Test
    default void allConnectionsCanBeQueried(final WebsocketRegistry websocketRegistry) {
        final ConnectionInformation connectionInformation = connectionInformation();
        final WebsocketRegistryEntry entry = websocketRegistryEntry(
                connectionInformation,
                websocketSenderId("foo"),
                headers(emptyList()),
                json(),
                queryParameters(emptyList())
        );
        websocketRegistry.addConnection(entry);
        final List<WebsocketRegistryEntry> connections = websocketRegistry.connections();
        assertThat(connections.size(), is(1));
        final WebsocketRegistryEntry queriedEntry = connections.get(0);
        assertThat(queriedEntry.getSenderId().asString(), is("foo"));
    }
}
