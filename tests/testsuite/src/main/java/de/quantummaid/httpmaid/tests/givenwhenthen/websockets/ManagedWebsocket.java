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

import de.quantummaid.httpmaid.tests.givenwhenthen.client.WrappedWebsocket;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static de.quantummaid.httpmaid.tests.givenwhenthen.Poller.pollWithTimeout;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ManagedWebsocket {
    @Getter
    @Setter
    private WrappedWebsocket websocket;
    @Getter
    @Setter
    private WebsocketStatus status;
    private final List<String> receivedMessages = new ArrayList<>();

    public static ManagedWebsocket managedWebsocket() {
        return new ManagedWebsocket();
    }

    public synchronized void addMessage(final String message) {
        receivedMessages.add(message);
    }

    public boolean waitAndCheckForMessageReceived(final String message) {
        return waitAndCheckForMessageReceived(message::equals);
    }

    public boolean waitAndCheckForMessageReceived(final Predicate<String> checker) {
        return pollWithTimeout(() -> hasReceivedMessage(checker));
    }

    public synchronized boolean hasReceivedMessage(final Predicate<String> checker) {
        return receivedMessages.stream().anyMatch(checker);
    }
}
