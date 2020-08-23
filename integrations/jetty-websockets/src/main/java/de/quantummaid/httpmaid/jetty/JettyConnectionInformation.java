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

package de.quantummaid.httpmaid.jetty;

import de.quantummaid.httpmaid.websockets.sender.NonSerializableConnectionInformation;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.eclipse.jetty.websocket.api.Session;

import java.io.IOException;
import java.io.UncheckedIOException;

import static de.quantummaid.httpmaid.jetty.JettyEndpointException.jettyEndpointException;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class JettyConnectionInformation implements NonSerializableConnectionInformation {
    private final Session session;

    public static JettyConnectionInformation jettyConnectionInformation(final Session session) {
        return new JettyConnectionInformation(session);
    }

    @Override
    public void send(final String message) {
        try {
            session.getRemote().sendString(message);
        } catch (final IOException e) {
            throw jettyEndpointException(e);
        }
    }

    @Override
    public void disconnect() {
        try {
            session.disconnect();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
