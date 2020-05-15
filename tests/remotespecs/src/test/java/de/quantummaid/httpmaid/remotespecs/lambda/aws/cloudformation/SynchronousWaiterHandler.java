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

package de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation;

import com.amazonaws.AmazonWebServiceRequest;
import com.amazonaws.waiters.WaiterHandler;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import static java.lang.String.format;

@ToString
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class SynchronousWaiterHandler extends WaiterHandler<AmazonWebServiceRequest> {
    private final String description;
    private boolean success;
    private Exception exception;

    public static SynchronousWaiterHandler synchronousWaiterHandler(final String description) {
        return new SynchronousWaiterHandler(description, false, null);
    }

    @Override
    public synchronized void onWaitSuccess(final AmazonWebServiceRequest request) {
        success = true;
    }

    @Override
    public synchronized void onWaitFailure(final Exception exception) {
        this.exception = exception;
    }

    public synchronized void verifySuccessful() {
        if (!success) {
            throw new RuntimeException(format("Error waiting for %s", description), exception);
        }
    }
}
