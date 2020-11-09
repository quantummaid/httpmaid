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

package de.quantummaid.httpmaid.websocketregistryspecs.remotedynamodb.aws.cloudformation;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.cloudformation.CloudFormationClient;
import software.amazon.awssdk.services.cloudformation.model.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static de.quantummaid.httpmaid.websocketregistryspecs.remotedynamodb.aws.cloudformation.CloudFormationWaiter.*;
import static java.lang.String.format;
import static software.amazon.awssdk.services.cloudformation.model.StackStatus.CREATE_COMPLETE;
import static software.amazon.awssdk.services.cloudformation.model.StackStatus.UPDATE_COMPLETE;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class CloudFormationHandler implements AutoCloseable {
    private final CloudFormationClient cloudFormationClient;

    public static CloudFormationHandler connectToCloudFormation() {
        final CloudFormationClient cloudFormationClient = CloudFormationClient.create();
        return new CloudFormationHandler(cloudFormationClient);
    }

    public void createOrUpdateStack(final String stackName,
                                    final String pathToTemplate,
                                    final Map<String, String> stackParameters) {
        try {
            createStack(stackName, pathToTemplate, stackParameters);
        } catch (final AlreadyExistsException e) {
            log.info("Stack {} already exists, updating instead.", stackName);
            updateStack(stackName, pathToTemplate, stackParameters);
        }
    }

    public void createStack(final String stackIdentifier,
                            final String pathToTemplate,
                            final Map<String, String> stackParameters) {
        log.info("Creating stack {}...", stackIdentifier);
        final String templateBody = fileToString(pathToTemplate);

        final CreateStackRequest createStackRequest = CreateStackRequest.builder()
                .stackName(stackIdentifier)
                .capabilities(Capability.CAPABILITY_NAMED_IAM)
                .templateBody(templateBody)
                .parameters(stackParameters.entrySet()
                        .stream()
                        .map(
                                kv -> Parameter.builder()
                                        .parameterKey(kv.getKey())
                                        .parameterValue(kv.getValue())
                                        .build()).collect(Collectors.toList()
                        ))
                .build();
        cloudFormationClient.createStack(createStackRequest);

        waitForStackCreation(stackIdentifier, cloudFormationClient);
        log.info("Created stack {}.", stackIdentifier);
    }

    public void updateStack(final String stackIdentifier,
                            final String pathToTemplate,
                            final Map<String, String> stackParameters) {
        log.info("Updating stack {}...", stackIdentifier);
        final String templateBody = fileToString(pathToTemplate);
        final UpdateStackRequest updateStackRequest = UpdateStackRequest.builder()
                .stackName(stackIdentifier)
                .capabilities(Capability.CAPABILITY_NAMED_IAM)
                .templateBody(templateBody)
                .parameters(stackParameters.entrySet().stream().map(
                        kv -> Parameter.builder()
                                .parameterKey(kv.getKey())
                                .parameterValue(kv.getValue())
                                .build()).collect(Collectors.toList()))
                .build();

        try {
            cloudFormationClient.updateStack(updateStackRequest);
        } catch (final CloudFormationException e) {
            final String message = e.getMessage();
            if (message.contains("No updates are to be performed.")) {
                log.info("Stack {} was already up to date.", stackIdentifier);
                return;
            } else {
                throw new CloudFormationHandlerException(
                        format("Exception thrown during update of stack %s", stackIdentifier), e);
            }
        }
        waitForStackUpdate(stackIdentifier, cloudFormationClient);
        log.info("Updated stack {}.", stackIdentifier);
    }

    public void deleteStacksStartingWith(final String stackPrefix) {
        final ListStacksResponse listStacksResponse = cloudFormationClient.listStacks();
        listStacksResponse.stackSummaries().stream()
                .filter(stack ->
                        stack.stackStatus().equals(CREATE_COMPLETE) ||
                                stack.stackStatus().equals(UPDATE_COMPLETE))
                .map(StackSummary::stackName)
                .filter(stackName -> stackName.startsWith(stackPrefix))
                .forEach(this::deleteStack);
    }

    private void deleteStack(final String stackIdentifier) {
        log.info("Deleting stack {}...", stackIdentifier);
        final DeleteStackRequest deleteStackRequest = DeleteStackRequest.builder()
                .stackName(stackIdentifier)
                .build();
        cloudFormationClient.deleteStack(deleteStackRequest);
        waitForStackDeletion(stackIdentifier, cloudFormationClient);
        log.info("Deleted stack {}.", stackIdentifier);
    }

    private static String fileToString(final String filePath) {
        final StringBuilder contentBuilder = new StringBuilder();
        try (Stream<String> stream = Files.lines(Paths.get(filePath), StandardCharsets.UTF_8)) {
            stream.forEach(s -> contentBuilder.append(s).append("\n"));
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
        return contentBuilder.toString();
    }

    @Override
    public void close() {
        cloudFormationClient.close();
    }
}
