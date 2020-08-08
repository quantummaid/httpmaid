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

package de.quantummaid.httpmaid.client.websocket.real;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static de.quantummaid.httpmaid.client.HttpMaidClientException.httpMaidClientException;
import static de.quantummaid.httpmaid.client.websocket.real.ConnectionResult.error;
import static de.quantummaid.httpmaid.client.websocket.real.ConnectionResult.success;
import static java.lang.Thread.currentThread;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
final class RealWebsocketState {
    private final CompletableFuture<ConnectionResult> connectionResultFuture = new CompletableFuture<>();

    static RealWebsocketState initialWebsocketState() {
        return new RealWebsocketState();
    }

    boolean isConnected() {
        return connectionResultFuture.isDone();
    }

    void errorOnConnectOccurred(final Throwable error) {
        connectionResultFuture.complete(error(error));
    }

    void setConnected() {
        if (isConnected()) {
            throw httpMaidClientException("already connected");
        }
        connectionResultFuture.complete(success());
    }

    void awaitConnect() {
        try {
            final ConnectionResult connectionResult = connectionResultFuture.get();
            if (!connectionResult.successful()) {
                throw httpMaidClientException("unexpected connect error", connectionResult.errorHasOccurred());
            }
        } catch (final InterruptedException e) {
            currentThread().interrupt();
            throw httpMaidClientException("interrupted connect", e);
        } catch (final ExecutionException e) {
            throw httpMaidClientException(e);
        }
    }
}
