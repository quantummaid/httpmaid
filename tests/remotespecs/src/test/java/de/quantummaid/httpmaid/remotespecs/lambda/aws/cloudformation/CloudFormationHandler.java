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
import com.amazonaws.services.cloudformation.AmazonCloudFormationClientBuilder;
import com.amazonaws.services.cloudformation.model.*;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation.CloudFormationWaiter.*;
import static java.lang.String.format;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class CloudFormationHandler implements AutoCloseable {
    private final AmazonCloudFormation amazonCloudFormation;

    public static CloudFormationHandler connectToCloudFormation() {
        final AmazonCloudFormation amazonCloudFormation = AmazonCloudFormationClientBuilder.defaultClient();
        return new CloudFormationHandler(amazonCloudFormation);
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
        final CreateStackRequest createStackRequest = new CreateStackRequest();
        createStackRequest.setStackName(stackIdentifier);
        createStackRequest.setCapabilities(List.of("CAPABILITY_NAMED_IAM"));
        createStackRequest.setTemplateBody(templateBody);
        createStackRequest.setParameters(
                stackParameters.entrySet().stream().map(
                        kv -> {
                            final Parameter param = new Parameter();
                            param.setParameterKey(kv.getKey());
                            param.setParameterValue(kv.getValue());
                            return param;
                        }).collect(Collectors.toList())
        );

        amazonCloudFormation.createStack(createStackRequest);
        waitForStackCreation(stackIdentifier, amazonCloudFormation);
        log.info("Created stack {}.", stackIdentifier);
    }

    public void updateStack(final String stackIdentifier,
                            final String pathToTemplate,
                            final Map<String, String> stackParameters) {
        log.info("Updating stack {}...", stackIdentifier);
        final String templateBody = fileToString(pathToTemplate);
        final UpdateStackRequest updateStackRequest = new UpdateStackRequest();
        updateStackRequest.setStackName(stackIdentifier);
        updateStackRequest.setCapabilities(List.of("CAPABILITY_NAMED_IAM"));
        updateStackRequest.setTemplateBody(templateBody);
        updateStackRequest.setParameters(
                stackParameters.entrySet().stream().map(
                        kv -> {
                            final Parameter param = new Parameter();
                            param.setParameterKey(kv.getKey());
                            param.setParameterValue(kv.getValue());
                            return param;
                        }).collect(Collectors.toList())
        );

        try {
            amazonCloudFormation.updateStack(updateStackRequest);
        } catch (final AmazonCloudFormationException e) {
            final String message = e.getMessage();
            if (message.contains("No updates are to be performed.")) {
                log.info("Stack {} was already up to date.", stackIdentifier);
                return;
            } else {
                throw new CloudFormationHandlerException(
                        format("Exception thrown during update of stack %s", stackIdentifier), e);
            }
        }
        waitForStackUpdate(stackIdentifier, amazonCloudFormation);
        log.info("Updated stack {}.", stackIdentifier);
    }

    public void deleteStacksStartingWith(final String stackPrefix) {
        final ListStacksResult listStacksResult = amazonCloudFormation.listStacks();
        listStacksResult.getStackSummaries().stream()
                .filter(stack -> stack.getStackStatus().equals("CREATE_COMPLETE"))
                .filter(stack -> stack.getStackName().startsWith(stackPrefix))
                .forEach(stack -> {
                    final String stackName = stack.getStackName();
                    deleteStack(stackName);
                });
    }

    private void deleteStack(final String stackIdentifier) {
        log.info("Deleting stack {}...", stackIdentifier);
        final DeleteStackRequest deleteStackRequest = new DeleteStackRequest();
        deleteStackRequest.setStackName(stackIdentifier);
        amazonCloudFormation.deleteStack(deleteStackRequest);
        waitForStackDeletion(stackIdentifier, amazonCloudFormation);
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
        amazonCloudFormation.shutdown();
    }
}
