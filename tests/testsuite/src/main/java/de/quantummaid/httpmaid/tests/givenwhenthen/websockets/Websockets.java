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

package de.quantummaid.httpmaid.tests.givenwhenthen.websockets;

import de.quantummaid.httpmaid.tests.givenwhenthen.Poller;
import de.quantummaid.httpmaid.tests.givenwhenthen.client.WrappedWebsocket;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Websockets {
    private final List<ManagedWebsocket> websockets;

    public static Websockets emptyWebsockets() {
        return new Websockets(new ArrayList<>());
    }

    public void addWebsocket(final ManagedWebsocket websocket) {
        websockets.add(websocket);
    }

    public WrappedWebsocket latestWebsocket() {
        final int index = websockets.size() - 1;
        return websockets.get(index).getWebsocket();
    }

    public boolean allAreClosed() {
        return websockets.stream()
                .map(ManagedWebsocket::getStatus)
                .allMatch(WebsocketStatus.CLOSED::equals);
    }

    public List<ManagedWebsocket> all() {
        return websockets;
    }

    public boolean waitForAllAreClosed() {
        return Poller.pollWithTimeout(this::allAreClosed);
    }
}
