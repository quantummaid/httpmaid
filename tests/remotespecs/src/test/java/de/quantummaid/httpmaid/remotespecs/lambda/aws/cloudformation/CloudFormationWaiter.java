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

import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.model.DescribeStacksRequest;
import com.amazonaws.waiters.Waiter;
import com.amazonaws.waiters.WaiterParameters;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation.SynchronousWaiterHandler.synchronousWaiterHandler;
import static java.lang.String.format;

@Slf4j
public final class CloudFormationWaiter {

    private CloudFormationWaiter() {
    }

    public static void waitForStackUpdate(final String stackIdentifier,
                                          final AmazonCloudFormation amazonCloudFormation) {
        final Waiter<DescribeStacksRequest> waiter = amazonCloudFormation.waiters().stackUpdateComplete();
        final String description = format("update of stack '%s'", stackIdentifier);
        wait(waiter, stackIdentifier, description);
    }

    public static void waitForStackCreation(final String stackIdentifier,
                                            final AmazonCloudFormation amazonCloudFormation) {
        final Waiter<DescribeStacksRequest> waiter = amazonCloudFormation.waiters().stackCreateComplete();
        final String description = format("creation of stack '%s'", stackIdentifier);
        wait(waiter, stackIdentifier, description);
    }

    public static void waitForStackDeletion(final String stackIdentifier,
                                            final AmazonCloudFormation amazonCloudFormation) {
        final Waiter<DescribeStacksRequest> waiter = amazonCloudFormation.waiters().stackDeleteComplete();
        final String description = format("deletion of stack '%s'", stackIdentifier);
        wait(waiter, stackIdentifier, description);
    }

    private static void wait(final Waiter<DescribeStacksRequest> waiter,
                             final String stackIdentifier,
                             final String description) {
        log.info("Waiting for {}...", description);
        final DescribeStacksRequest request = new DescribeStacksRequest().withStackName(stackIdentifier);
        final SynchronousWaiterHandler waiterHandler = synchronousWaiterHandler(description);
        final Future<Void> waitFuture = waiter.runAsync(new WaiterParameters<>(request), waiterHandler);

        try {
            waitFuture.get(4, TimeUnit.MINUTES);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (final ExecutionException | TimeoutException e) {
            throw new RuntimeException(e);
        }
        waiterHandler.verifySuccessful();
        log.info("Succeesfully waited for {}.", description);
    }
}
