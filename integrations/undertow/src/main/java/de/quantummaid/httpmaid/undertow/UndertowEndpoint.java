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
import io.undertow.Handlers;
import io.undertow.Undertow;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import static de.quantummaid.httpmaid.closing.ClosingActions.CLOSING_ACTIONS;
import static de.quantummaid.httpmaid.undertow.UndertowHandler.undertowHandler;
import static de.quantummaid.httpmaid.undertow.UndertowWebsocketsCallback.undertowWebsocketsCallback;
import static de.quantummaid.httpmaid.util.Validators.validateNotNull;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class UndertowEndpoint implements AutoCloseable {
    private final HttpMaid httpMaid;

    public static UndertowEndpoint startUndertowEndpoint(final HttpMaid httpMaid,
                                                         final int port) {
        validateNotNull(httpMaid, "httpMaid");
        final Undertow undertow = Undertow.builder()
                .addHttpListener(port, "localhost")
                .setHandler(Handlers.websocket(
                        undertowWebsocketsCallback(httpMaid),
                        undertowHandler(httpMaid))
                )
                //.setHandler(undertowHandler(httpMaid))
                .build();
        undertow.start();
        httpMaid.getMetaDatum(CLOSING_ACTIONS).addClosingAction(undertow::stop);
        return new UndertowEndpoint(httpMaid);
    }

    @Override
    public void close() {
        httpMaid.close();
    }
}
