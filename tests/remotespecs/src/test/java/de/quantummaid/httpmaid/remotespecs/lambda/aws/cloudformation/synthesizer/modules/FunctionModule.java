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

import de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation.synthesizer.*;
import de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation.synthesizer.resources.Lambda;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.Map;

import static de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation.synthesizer.JarCoordinates.jarCoordinates;
import static de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation.synthesizer.resources.Iam.apiGatewayPermission;
import static de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation.synthesizer.resources.Iam.iamRole;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class FunctionModule implements CloudformationModule {
    private final CloudformationResource functionRole;
    private final CloudformationResource function;
    private final CloudformationResource functionResourcePermission;

    public static FunctionModule unauthorizedFunctionModule(final Namespace namespace,
                                                            final String bucketName,
                                                            final String artifactKey,
                                                            final Map<String, Object> environment) {
        return functionModule(
                namespace,
                bucketName,
                artifactKey,
                "de.quantummaid.httpmaid.testlambda.UnauthorizedLambda",
                "handleRequest",
                environment
        );
    }

    public static FunctionModule dummyAuthorizedFunctionModule(final Namespace namespace,
                                                               final String bucketName,
                                                               final String artifactKey,
                                                               final Map<String, Object> environment) {
        return functionModule(
                namespace,
                bucketName,
                artifactKey,
                "de.quantummaid.httpmaid.testlambda.DummyAuthorizerLambda",
                "handleRequest",
                environment
        );
    }

    public static FunctionModule cognitoAuthorizedFunctionModule(final Namespace namespace,
                                                                 final String bucketName,
                                                                 final String artifactKey,
                                                                 final Map<String, Object> environment) {
        return functionModule(
                namespace,
                bucketName,
                artifactKey,
                "de.quantummaid.httpmaid.testlambda.CognitoAuthorizerLambda",
                "handleRequest",
                environment
        );
    }

    public static FunctionModule functionModule(final Namespace namespace,
                                                final String bucketName,
                                                final String artifactKey,
                                                final String handlerClass,
                                                final String handlerMethod,
                                                final Map<String, Object> environment) {
        final CloudformationResource functionRole = iamRole(
                namespace.id("FunctionRole"),
                namespace.id("FunctionRole"),
                namespace.sub("FunctionRole")
        );
        final JarCoordinates jarCoordinates = jarCoordinates(bucketName, artifactKey);
        final CloudformationResource function = Lambda.function(
                namespace.id("Function"),
                namespace.id("Function"),
                jarCoordinates,
                functionRole,
                handlerClass,
                handlerMethod,
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
}
