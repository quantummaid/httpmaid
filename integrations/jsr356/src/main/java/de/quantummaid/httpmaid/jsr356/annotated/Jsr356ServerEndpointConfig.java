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
import de.quantummaid.httpmaid.jsr356.HandshakeMetaData;
import de.quantummaid.httpmaid.jsr356.programmatic.ProgrammaticJsr356Endpoint;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import javax.websocket.Decoder;
import javax.websocket.Encoder;
import javax.websocket.Extension;
import javax.websocket.server.ServerEndpointConfig;
import java.util.List;
import java.util.Map;

import static de.quantummaid.httpmaid.jsr356.HandshakeMetaData.handshakeMetaData;
import static de.quantummaid.httpmaid.jsr356.programmatic.Jsr356EndpointConfigurator.jsr356EndpointConfigurator;
import static java.util.Collections.emptyList;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Jsr356ServerEndpointConfig implements ServerEndpointConfig {
    private final HttpMaid httpMaid;
    private final HandshakeMetaData handshakeMetaData;

    public static ServerEndpointConfig jsr356ServerEndpointConfig(final HttpMaid httpMaid) {
        final HandshakeMetaData handshakeMetaData = handshakeMetaData();
        return new Jsr356ServerEndpointConfig(httpMaid, handshakeMetaData);
    }

    @Override
    public Class<?> getEndpointClass() {
        return ProgrammaticJsr356Endpoint.class;
    }

    @Override
    public String getPath() {
        return "/";
    }

    @Override
    public List<String> getSubprotocols() {
        return emptyList();
    }

    @Override
    public List<Extension> getExtensions() {
        return emptyList();
    }

    @Override
    public Configurator getConfigurator() {
        return jsr356EndpointConfigurator(httpMaid, handshakeMetaData);
    }

    @Override
    public List<Class<? extends Encoder>> getEncoders() {
        return emptyList();
    }

    @Override
    public List<Class<? extends Decoder>> getDecoders() {
        return emptyList();
    }

    @Override
    public Map<String, Object> getUserProperties() {
        return Map.of();
    }
}
