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

package de.quantummaid.httpmaid.awslambda.sender.apigateway.async;

import de.quantummaid.httpmaid.awslambda.sender.apigateway.GatewayOperation;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import static java.util.concurrent.TimeUnit.SECONDS;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class FutureGatewayOperation implements GatewayOperation {
    private static final int TIMEOUT_IN_SECONDS = 10;
    private final CompletableFuture<?> future;

    public static GatewayOperation futureGatewayOperation(final CompletableFuture<?> future) {
        return new FutureGatewayOperation(future);
    }

    @Override
    public void awaitResult(final Consumer<Throwable> onException) {
        try {
            future.get(TIMEOUT_IN_SECONDS, SECONDS);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("interrupted during wait for future", e);
        } catch (final TimeoutException e) {
            log.warn("timed out waiting for future", e);
            future.cancel(true);
            onException.accept(e);
        } catch (final ExecutionException e) {
            final Throwable cause = e.getCause();
            onException.accept(cause);
        }
    }
}
