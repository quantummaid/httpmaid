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

package de.quantummaid.httpmaid.jsr356.programmatic;

import de.quantummaid.httpmaid.HttpMaid;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import javax.websocket.MessageHandler;
import javax.websocket.Session;
import java.io.IOException;

import static de.quantummaid.httpmaid.jsr356.Jsr356Exception.jsr356Exception;
import static de.quantummaid.httpmaid.websockets.endpoint.RawWebsocketMessage.rawWebsocketMessage;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Jsr356MessageHandler implements MessageHandler.Whole<String> {
    private final Session session;
    private final HttpMaid httpMaid;

    public static MessageHandler.Whole<String> jsr356MessageHandler(final Session session,
                                                                    final HttpMaid httpMaid) {
        return new Jsr356MessageHandler(session, httpMaid);
    }

    @Override
    public void onMessage(final String message) {
        httpMaid.handleRequest(
                () -> rawWebsocketMessage(session, message),
                response -> response.optionalStringBody()
                        .ifPresent(responseMessage -> sendMessage(session, responseMessage))
        );
    }

    private void sendMessage(final Session session,
                             final String message) {
        try {
            session.getBasicRemote().sendText(message);
        } catch (final IOException e) {
            throw jsr356Exception(e);
        }
    }
}
