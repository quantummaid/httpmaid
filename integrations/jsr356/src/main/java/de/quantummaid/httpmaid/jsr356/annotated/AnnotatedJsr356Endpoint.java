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

package de.quantummaid.httpmaid.jsr356.annotated;

import de.quantummaid.httpmaid.HttpMaid;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import javax.websocket.EndpointConfig;
import javax.websocket.Session;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static de.quantummaid.httpmaid.jsr356.Jsr356Exception.jsr356Exception;
import static de.quantummaid.httpmaid.websockets.endpoint.RawWebsocketConnect.rawWebsocketConnect;
import static de.quantummaid.httpmaid.websockets.endpoint.RawWebsocketMessage.rawWebsocketMessage;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
//@ServerEndpoint("/")
public class AnnotatedJsr356Endpoint {
    private final HttpMaid httpMaid;
    private final Map<String, List<String>> headers;

    public static AnnotatedJsr356Endpoint annotatedJsr356Endpoint(final HttpMaid httpMaid,
                                                                  final Map<String, List<String>> headers) {
        return new AnnotatedJsr356Endpoint(httpMaid, headers);
    }

    //@OnOpen
    public void onOpen(final Session session,
                       final EndpointConfig endpointConfig) throws IOException {
        httpMaid.handleRequest(() -> {
            final Map<String, String> queryParameters = session.getPathParameters();
            return rawWebsocketConnect(session, queryParameters, headers);
        }, response -> {
        });
    }

    //@OnMessage
    public void onMessage(final String message, final Session session) {
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
