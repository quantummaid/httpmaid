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
import de.quantummaid.httpmaid.remotespecs.lambda.ResourcesLog;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

import static de.quantummaid.httpmaid.remotespecs.lambda.ResourcesLog.resourcesLog;
import static de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation.CloudFormationWaiter.waitForStackCreation;
import static de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation.CloudFormationWaiter.waitForStackDeletion;

@Slf4j
public final class CloudFormationHandler {
    private static final ResourcesLog RESOURCES_LOG = resourcesLog();

    private CloudFormationHandler() {
    }

    public static void createStack(final String stackIdentifier,
                                   final String pathToTemplate) {
        log.info("Creating stack {}...", stackIdentifier);
        final String templateBody = fileToString(pathToTemplate);
        final AmazonCloudFormation amazonCloudFormation = AmazonCloudFormationClientBuilder.defaultClient();
        try {
            final CreateStackRequest createStackRequest = new CreateStackRequest();
            createStackRequest.setStackName(stackIdentifier);
            createStackRequest.setCapabilities(List.of("CAPABILITY_NAMED_IAM"));
            createStackRequest.setTemplateBody(templateBody);

            final Parameter parameter = new Parameter();
            parameter.setParameterKey("StackIdentifier");
            parameter.setParameterValue(stackIdentifier);
            createStackRequest.setParameters(List.of(parameter));

            amazonCloudFormation.createStack(createStackRequest);
            waitForStackCreation(stackIdentifier, amazonCloudFormation);

            final DescribeStackResourcesRequest describeStackResourcesRequest = new DescribeStackResourcesRequest().withStackName(stackIdentifier);
            final DescribeStackResourcesResult stackResources = amazonCloudFormation.describeStackResources(describeStackResourcesRequest);
            RESOURCES_LOG.log(stackResources.getStackResources());
            RESOURCES_LOG.dump();
        } finally {
            amazonCloudFormation.shutdown();
        }
        log.info("Created stack {}.", stackIdentifier);
    }

    public static void main(String[] args) {
        deleteStacksStartingWith("remotespecs");
    }

    public static void deleteStacksStartingWith(final String stackPrefix) {
        final AmazonCloudFormation amazonCloudFormation = AmazonCloudFormationClientBuilder.defaultClient();
        try {

            final ListStacksResult listStacksResult = amazonCloudFormation.listStacks();
            listStacksResult.getStackSummaries().stream()
                    .filter(stack -> stack.getStackStatus().equals("CREATE_COMPLETE"))
                    .filter(stack -> stack.getStackName().startsWith(stackPrefix))
                    .forEach(stack -> {
                        final String stackName = stack.getStackName();
                        deleteStack(stackName, amazonCloudFormation);
                    });
        } finally {
            amazonCloudFormation.shutdown();
        }
    }

    private static void deleteStack(final String stackIdentifier,
                                    final AmazonCloudFormation amazonCloudFormation) {
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
}
