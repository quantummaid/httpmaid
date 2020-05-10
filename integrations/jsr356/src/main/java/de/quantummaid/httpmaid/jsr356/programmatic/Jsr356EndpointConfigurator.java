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
import de.quantummaid.httpmaid.jsr356.HandshakeMetaData;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;
import java.util.List;
import java.util.Map;

import static de.quantummaid.httpmaid.jsr356.programmatic.ProgrammaticJsr356Endpoint.programmaticJsr356Endpoint;

@ToString
@EqualsAndHashCode(callSuper = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Jsr356EndpointConfigurator extends ServerEndpointConfig.Configurator {
    private final HttpMaid httpMaid;
    private final HandshakeMetaData handshakeMetaData;

    public static ServerEndpointConfig.Configurator jsr356EndpointConfigurator(final HttpMaid httpMaid,
                                                                               final HandshakeMetaData handshakeMetaData) {
        return new Jsr356EndpointConfigurator(httpMaid, handshakeMetaData);
    }

    @SuppressWarnings("unchecked")
    @Override
    public synchronized <T> T getEndpointInstance(final Class<T> endpointClass) {
        final Map<String, List<String>> headers = handshakeMetaData.getHeaders();
        return (T) programmaticJsr356Endpoint(httpMaid, headers);
    }

    @Override
    public synchronized void modifyHandshake(final ServerEndpointConfig serverEndpointConfig,
                                             final HandshakeRequest request,
                                             final HandshakeResponse response) {
        final Map<String, List<String>> headers = request.getHeaders();
        handshakeMetaData.setHeaders(headers);
    }
}
