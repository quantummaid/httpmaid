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

package de.quantummaid.httpmaid.awslambda;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.BiConsumer;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class ConnectionFuture {
    private final AwsWebsocketConnectionInformation connectionInformation;
    private final CompletableFuture<?> future;

    public static ConnectionFuture connectionFuture(final AwsWebsocketConnectionInformation connectionInformation,
                                                    final CompletableFuture<?> future) {
        return new ConnectionFuture(connectionInformation, future);
    }

    public boolean isDone() {
        return future.isDone();
    }

    public void check(final BiConsumer<AwsWebsocketConnectionInformation, Throwable> onException) {
        try {
            future.get();
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Interrupted during wait for future", e);
        } catch (final ExecutionException e) {
            onException.accept(connectionInformation, e.getCause());
        }
    }
}
