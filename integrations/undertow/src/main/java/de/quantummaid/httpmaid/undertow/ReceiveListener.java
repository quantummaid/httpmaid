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

import de.quantummaid.httpmaid.HttpMaid;
import de.quantummaid.httpmaid.websockets.endpoint.RawWebsocketMessage;
import de.quantummaid.httpmaid.websockets.sender.NonSerializableConnectionInformation;
import io.undertow.websockets.core.AbstractReceiveListener;
import io.undertow.websockets.core.BufferedTextMessage;
import io.undertow.websockets.core.WebSocketChannel;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import static io.undertow.websockets.core.WebSockets.sendText;

@ToString
@EqualsAndHashCode(callSuper = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ReceiveListener extends AbstractReceiveListener {
    private final NonSerializableConnectionInformation connectionInformation;
    private final HttpMaid httpMaid;

    public static ReceiveListener receiveListener(final NonSerializableConnectionInformation connectionInformation,
                                                  final HttpMaid httpMaid) {
        return new ReceiveListener(connectionInformation, httpMaid);
    }

    @Override
    protected void onFullTextMessage(final WebSocketChannel channel,
                                     final BufferedTextMessage message) {
        httpMaid.handleRequest(
                () -> {
                    final String messageData = message.getData();
                    return RawWebsocketMessage.rawWebsocketMessage(connectionInformation, messageData);
                },
                response -> response.optionalStringBody()
                        .ifPresent(responseMessage -> sendText(responseMessage, channel, null))
        );
    }
}
