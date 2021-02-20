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

package de.quantummaid.httpmaid.awslambda.sender.apigateway.sync;

import de.quantummaid.httpmaid.awslambda.sender.apigateway.GatewayOperation;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.function.Consumer;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class AlreadyExecutedGatewayOperation implements GatewayOperation {
    private final Exception exception;

    public static AlreadyExecutedGatewayOperation executeSynchronouslyNow(final Runnable operation) {
        try {
            operation.run();
            return successfulGatewayOperation();
        } catch (final Exception e) {
            return failedGatewayOperation(e);
        }
    }

    public static AlreadyExecutedGatewayOperation successfulGatewayOperation() {
        return new AlreadyExecutedGatewayOperation(null);
    }

    public static AlreadyExecutedGatewayOperation failedGatewayOperation(final Exception e) {
        return new AlreadyExecutedGatewayOperation(e);
    }

    @Override
    public void awaitResult(final Consumer<Throwable> onException) {
        if (exception != null) {
            onException.accept(exception);
        }
    }
}
