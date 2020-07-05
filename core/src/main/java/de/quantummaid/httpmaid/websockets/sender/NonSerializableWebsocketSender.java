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

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.List;
import java.util.function.BiConsumer;

import static de.quantummaid.httpmaid.websockets.sender.WebsocketSenderId.websocketSenderId;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class NonSerializableWebsocketSender implements WebsocketSender<NonSerializableConnectionInformation> {
    public static final WebsocketSenderId NON_SERIALIZABLE_WEBSOCKET_SENDER = websocketSenderId("NON_SERIALIZABLE_WEBSOCKET_SENDER");

    public static NonSerializableWebsocketSender nonSerializableWebsocketSender() {
        return new NonSerializableWebsocketSender();
    }

    @Override
    public void send(final String message,
                     final List<NonSerializableConnectionInformation> connectionInformations,
                     final BiConsumer<NonSerializableConnectionInformation, Throwable> onException) {
        connectionInformations.forEach(connectionInformation -> {
            try {
                connectionInformation.send(message);
            } catch (final Exception e) {
                onException.accept(connectionInformation, e);
            }
        });
    }

    @Override
    public void disconnect(final List<NonSerializableConnectionInformation> connectionInformations,
                           final BiConsumer<NonSerializableConnectionInformation, Throwable> onException) {
        connectionInformations.forEach(connectionInformation -> {
            try {
                connectionInformation.disconnect();
            } catch (final Exception e) {
                onException.accept(connectionInformation, e);
            }
        });
    }

    @Override
    public WebsocketSenderId senderId() {
        return NON_SERIALIZABLE_WEBSOCKET_SENDER;
    }
}
