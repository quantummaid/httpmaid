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

package de.quantummaid.httpmaid.jsr356;

import de.quantummaid.httpmaid.HttpMaid;
import de.quantummaid.httpmaid.http.Headers;
import de.quantummaid.httpmaid.websockets.endpoint.RawWebsocketConnectBuilder;
import de.quantummaid.httpmaid.websockets.sender.NonSerializableConnectionInformation;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.Session;

import static de.quantummaid.httpmaid.jsr356.Jsr356MessageHandler.jsr356MessageHandler;
import static de.quantummaid.httpmaid.jsr356.SenderHelper.sendMessage;
import static de.quantummaid.httpmaid.websockets.endpoint.RawWebsocketConnect.rawWebsocketConnectBuilder;

@ToString
@EqualsAndHashCode(callSuper = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Jsr356Endpoint extends Endpoint {
    private final HttpMaid httpMaid;
    private final Headers headers;

    public static Jsr356Endpoint programmaticJsr356Endpoint(final HttpMaid httpMaid,
                                                            final Headers headers) {
        return new Jsr356Endpoint(httpMaid, headers);
    }

    @Override
    public void onOpen(final Session session, final EndpointConfig config) {
        final NonSerializableConnectionInformation connectionInformation = message -> sendMessage(session, message);
        httpMaid.handleRequest(
                () -> {
                    final RawWebsocketConnectBuilder builder = rawWebsocketConnectBuilder();
                    builder.withNonSerializableConnectionInformation(connectionInformation);
                    builder.withHeaders(headers);
                    final String queryString = session.getQueryString();
                    builder.withEncodedQueryString(queryString);
                    return builder.build();
                },
                response -> {
                }
        );
        session.addMessageHandler(jsr356MessageHandler(connectionInformation, session, httpMaid));
    }
}
