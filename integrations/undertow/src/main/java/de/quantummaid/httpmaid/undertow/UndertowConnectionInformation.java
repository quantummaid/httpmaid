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

package de.quantummaid.httpmaid.undertow;

import de.quantummaid.httpmaid.websockets.sender.NonSerializableConnectionInformation;
import io.undertow.websockets.core.WebSocketChannel;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.io.IOException;

import static de.quantummaid.httpmaid.undertow.UndertowEndpointException.undertowEndpointException;
import static io.undertow.websockets.core.WebSockets.sendText;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class UndertowConnectionInformation implements NonSerializableConnectionInformation {
    private final WebSocketChannel channel;

    public static UndertowConnectionInformation undertowConnectionInformation(final WebSocketChannel channel) {
        return new UndertowConnectionInformation(channel);
    }

    @Override
    public void send(final String message) {
        sendText(message, channel, null);
    }

    @Override
    public void disconnect() {
        try {
            channel.close();
        } catch (final IOException e) {
            throw undertowEndpointException(e);
        }
    }
}
