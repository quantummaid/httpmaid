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

package de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation.synthesizer.resources;

import de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation.synthesizer.CloudformationResource;
import de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation.synthesizer.Namespace;

import java.util.List;
import java.util.Map;

import static de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation.synthesizer.IntrinsicFunctions.sub;
import static java.lang.String.format;

public final class Iam {
    private static final String VERSION = "2012-10-17";

    private Iam() {
    }

    public static CloudformationResource apiGatewayPermission(final String resourceId, final CloudformationResource function) {
        return CloudformationResource.cloudformationResource(
                resourceId, "AWS::Lambda::Permission", Map.of(
                        "Action", "lambda:invokeFunction",
                        "Principal", "apigateway.amazonaws.com",
                        "FunctionName", function.reference()
                )
        );
    }

    public static CloudformationResource functionPermission(final String resourceId,
                                                            final CloudformationResource function,
                                                            final CloudformationResource websocketsApi,
                                                            final CloudformationResource websocketsApiStage) {
        final Object sourceArn = sub(format("arn:aws:apigateway:${AWS::Region}::/restapis/${%s}/stages/${%s}",
                websocketsApi.name(),
                websocketsApiStage.name()));
        return CloudformationResource.cloudformationResource(
                resourceId, "AWS::Lambda::Permission", Map.of(
                        "Action", "lambda:invokeFunction",
                        "Principal", "apigateway.amazonaws.com",
                        "FunctionName", function.reference(),
                        "SourceArn", sourceArn
                )
        );
    }

    public static CloudformationResource iamRole(final String resourceId,
                                                 final String roleName,
                                                 final Namespace namespace) {
        return CloudformationResource.cloudformationResource(
                resourceId,
                "AWS::IAM::Role",
                Map.of(
                        "RoleName", roleName,
                        "ManagedPolicyArns", List.of("arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole"),
                        "Policies", List.of(
                                allowPolicy(namespace.id("FunctionRolePolicy"), List.of("sts:AssumeRole")),
                                allowPolicy(namespace.id("ExecuteApi"), List.of("execute-api:ManageConnections")),
                                allowPolicy(namespace.id("getuser"), List.of("cognito-idp:GetUser")),
                                allowPolicy(namespace.id("lambda"), List.of("lambda:InvokeFunction")),
                                allowPolicy(namespace.id("UseDBPolicy"), List.of(
                                        "dynamodb:DeleteItem",
                                        "dynamodb:GetItem",
                                        "dynamodb:PutItem",
                                        "dynamodb:Query",
                                        "dynamodb:Scan",
                                        "dynamodb:UpdateItem")
                                )
                        ),
                        "AssumeRolePolicyDocument", Map.of(
                                "Version", VERSION,
                                "Statement", List.of(
                                        Map.of(
                                                "Effect", "Allow",
                                                "Principal", Map.of(
                                                        "Service", List.of("lambda.amazonaws.com", "apigateway.amazonaws.com")
                                                ),
                                                "Action", List.of("sts:AssumeRole")
                                        )
                                )
                        )
                )
        );
    }

    private static Map<String, Object> allowPolicy(final String policyName,
                                                   final List<String> actions) {
        return Map.of(
                "PolicyName", policyName,
                "PolicyDocument", Map.of(
                        "Version", VERSION,
                        "Statement", List.of(
                                Map.of(
                                        "Action", actions,
                                        "Resource", "*",
                                        "Effect", "Allow"

                                )
                        )
                )
        );
    }
}
