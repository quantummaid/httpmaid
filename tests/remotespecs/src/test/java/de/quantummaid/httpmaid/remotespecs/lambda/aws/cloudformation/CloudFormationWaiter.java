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

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.cloudformation.CloudFormationClient;
import software.amazon.awssdk.services.cloudformation.model.DescribeStacksResponse;
import software.amazon.awssdk.services.cloudformation.model.Stack;
import software.amazon.awssdk.services.cloudformation.model.StackStatus;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static de.quantummaid.httpmaid.tests.givenwhenthen.Poller.pollWithTimeout;
import static java.util.stream.Collectors.toList;

@Slf4j
public final class CloudFormationWaiter {
    private static final int MAX_NUMBER_OF_TRIES = 50;
    private static final int SLEEP_TIME_IN_MILLISECONDS = 5000;

    private CloudFormationWaiter() {
    }

    public static void waitForStackUpdate(final String stackIdentifier,
                                          final CloudFormationClient cloudFormationClient) {
        waitForStatus(stackIdentifier, cloudFormationClient, StackStatus.UPDATE_COMPLETE);
    }

    public static void waitForStackCreation(final String stackIdentifier,
                                            final CloudFormationClient cloudFormationClient) {
        waitForStatus(stackIdentifier, cloudFormationClient, StackStatus.CREATE_COMPLETE);
    }

    public static void waitForStackDeletion(final String stackIdentifier,
                                            final CloudFormationClient cloudFormationClient) {
        waitFor(stackIdentifier, cloudFormationClient, stack -> stack
                .map(Stack::stackStatus)
                .map(stackStatus -> {
                    log.info("Waiting for stack {} to be deleted but it still exists in status {}",
                            stackIdentifier, stackStatus);
                    return false;
                })
                .orElse(true));
    }

    private static void waitForStatus(final String stackIdentifier,
                                      final CloudFormationClient cloudFormationClient,
                                      final StackStatus expectedStatus) {
        waitFor(stackIdentifier, cloudFormationClient, stack -> stack
                .map(Stack::stackStatus)
                .map(stackStatus -> {
                    final boolean equals = expectedStatus.equals(stackStatus);
                    if (!equals) {
                        log.info("Waiting for stack {} to become {} but was {}",
                                stackIdentifier, expectedStatus, stackStatus);
                    }
                    return equals;
                })
                .orElseGet(() -> {
                    log.info("Did not find stack {}", stackIdentifier);
                    return false;
                })
        );
    }

    private static void waitFor(final String stackIdentifier,
                                final CloudFormationClient cloudFormationClient,
                                final Predicate<Optional<Stack>> condition) {
        pollWithTimeout(MAX_NUMBER_OF_TRIES, SLEEP_TIME_IN_MILLISECONDS,
                () -> conditionReached(stackIdentifier, cloudFormationClient, condition));
    }

    private static boolean conditionReached(final String stackIdentifier,
                                            final CloudFormationClient cloudFormationClient,
                                            final Predicate<Optional<Stack>> condidtion) {
        final DescribeStacksResponse describeStacksResponse = cloudFormationClient.describeStacks();

        final List<Stack> stacks = describeStacksResponse.stacks().stream()
                .filter(stack -> stackIdentifier.equals(stack.stackName()))
                .collect(toList());
        final Optional<Stack> stack;
        if (stacks.isEmpty()) {
            stack = Optional.empty();
        } else {
            stack = Optional.of(stacks.get(0));
        }
        return condidtion.test(stack);
    }
}
