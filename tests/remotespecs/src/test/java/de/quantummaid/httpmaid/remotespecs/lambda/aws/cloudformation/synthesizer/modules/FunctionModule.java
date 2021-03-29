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

package de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation.synthesizer.modules;

import de.quantummaid.httpmaid.remotespecs.lambda.aws.Artifact;
import de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation.synthesizer.CloudformationModule;
import de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation.synthesizer.CloudformationResource;
import de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation.synthesizer.CloudformationTemplateBuilder;
import de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation.synthesizer.Namespace;
import de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation.synthesizer.resources.Lambda;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.Map;

import static de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation.synthesizer.resources.Iam.apiGatewayPermission;
import static de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation.synthesizer.resources.Iam.iamRole;
import static de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation.synthesizer.resources.Lambda.LambdaPayload.java11Payload;
import static de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation.synthesizer.resources.Lambda.LambdaPayload.providedPayload;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class FunctionModule implements CloudformationModule {
    private final CloudformationResource functionRole;
    private final CloudformationResource function;
    private final CloudformationResource functionResourcePermission;

    public static FunctionModule unauthorizedFunctionModule(final Namespace namespace,
                                                            final Artifact artifact,
                                                            final Map<String, Object> environment) {
        return functionModule(
                namespace,
                artifact,
                forClass("de.quantummaid.httpmaid.testlambda.UnauthorizedLambda"),
                environment
        );
    }

    public static FunctionModule graalVmUnauthorizedFunctionModule(final Namespace namespace,
                                                                   final Artifact artifact,
                                                                   final Map<String, Object> environment) {
        return functionModule(
                namespace,
                artifact,
                providedPayload("Unauthorized"),
                environment
        );
    }

    public static FunctionModule dummyAuthorizedFunctionModule(final Namespace namespace,
                                                               final Artifact artifact,
                                                               final Map<String, Object> environment) {
        return functionModule(
                namespace,
                artifact,
                forClass("de.quantummaid.httpmaid.testlambda.DummyAuthorizerLambda"),
                environment
        );
    }

    public static FunctionModule graalVmDummyAuthorizedFunctionModule(final Namespace namespace,
                                                                      final Artifact artifact,
                                                                      final Map<String, Object> environment) {
        return functionModule(
                namespace,
                artifact,
                providedPayload("DummyAuthorizer"),
                environment
        );
    }

    public static FunctionModule cognitoAuthorizedFunctionModule(final Namespace namespace,
                                                                 final Artifact artifact,
                                                                 final Map<String, Object> environment) {
        return functionModule(
                namespace,
                artifact,
                forClass("de.quantummaid.httpmaid.testlambda.CognitoAuthorizerLambda"),
                environment
        );
    }

    public static FunctionModule graalVmCognitoAuthorizedFunctionModule(final Namespace namespace,
                                                                        final Artifact artifact,
                                                                        final Map<String, Object> environment) {
        return functionModule(
                namespace,
                artifact,
                providedPayload("CognitoAuthorizer"),
                environment
        );
    }

    public static FunctionModule functionModule(final Namespace namespace,
                                                final Artifact artifact,
                                                final Lambda.LambdaPayload payload,
                                                final Map<String, Object> environment) {
        final CloudformationResource functionRole = iamRole(
                namespace.id("FunctionRole"),
                namespace.id("FunctionRole"),
                namespace.sub("FunctionRole")
        );
        final CloudformationResource function = Lambda.function(
                namespace.id("Function"),
                namespace.id("Function"),
                artifact,
                functionRole,
                payload,
                environment
        );
        final CloudformationResource functionResourcePermission = apiGatewayPermission(
                namespace.id("FunctionResourcePermission"),
                function
        );
        return new FunctionModule(functionRole, function, functionResourcePermission);
    }

    public CloudformationResource function() {
        return function;
    }

    public CloudformationResource role() {
        return functionRole;
    }

    @Override
    public void apply(final CloudformationTemplateBuilder builder) {
        builder.withResources(functionRole, function, functionResourcePermission);
    }

    private static Lambda.LambdaPayload forClass(final String className) {
        return java11Payload(
                className,
                "handleRequest"
        );
    }
}
