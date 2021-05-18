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

import de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation.synthesizer.CloudformationName;
import de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation.synthesizer.CloudformationResource;

import java.util.List;
import java.util.Map;

public final class ApiGatewayV1 {

    private ApiGatewayV1() {
    }

    public static CloudformationResource restApi(final CloudformationName resourceId) {
        return CloudformationResource.cloudformationResource(resourceId, "AWS::ApiGateway::RestApi", Map.of(
                "Name", resourceId.asId(),
                "FailOnWarnings", true
        ));
    }

    public static CloudformationResource resource(final CloudformationName resourceId, final CloudformationResource api) {
        return CloudformationResource.cloudformationResource(resourceId, "AWS::ApiGateway::Resource", Map.of(
                "RestApiId", api.reference(),
                "ParentId", api.attribute("RootResourceId"),
                "PathPart", "{path+}"
        ));
    }

    public static CloudformationResource anyMethod(final CloudformationName resourceId,
                                                   final CloudformationResource restApi,
                                                   final CloudformationResource resource,
                                                   final CloudformationResource function) {
        final Object invocationUri = ApiGatewayV2.buildInvocationUri(function);
        return CloudformationResource.cloudformationResource(resourceId, "AWS::ApiGateway::Method", Map.of(
                "AuthorizationType", "NONE",
                "HttpMethod", "ANY",
                "RestApiId", restApi.reference(),
                "ResourceId", resource.reference(),
                "ApiKeyRequired", false,
                "Integration", Map.of(
                        "Type", "AWS_PROXY",
                        "IntegrationHttpMethod", "POST",
                        "PassthroughBehavior", "WHEN_NO_MATCH",
                        "Uri", invocationUri
                )
        ));
    }

    public static CloudformationResource secondAnyMethod(final CloudformationName resourceId,
                                                         final CloudformationResource restApi,
                                                         final CloudformationResource function) {
        final Object invocationUri = ApiGatewayV2.buildInvocationUri(function);
        return CloudformationResource.cloudformationResource(resourceId, "AWS::ApiGateway::Method", Map.of(
                "AuthorizationType", "NONE",
                "HttpMethod", "ANY",
                "RestApiId", restApi.reference(),
                "ResourceId", restApi.attribute("RootResourceId"),
                "ApiKeyRequired", false,
                "Integration", Map.of(
                        "Type", "AWS_PROXY",
                        "IntegrationHttpMethod", "POST",
                        "PassthroughBehavior", "WHEN_NO_MATCH",
                        "Uri", invocationUri
                )
        ));
    }

    public static CloudformationResource restApiDeployment(final CloudformationName resourceId,
                                                           final CloudformationResource api,
                                                           final CloudformationResource method) {
        return CloudformationResource.cloudformationResource(resourceId, "AWS::ApiGateway::Deployment", List.of(method), Map.of(
                "RestApiId", api.reference()
        ));
    }

    public static CloudformationResource restApiStage(final CloudformationName resourceId,
                                                      final CloudformationResource api,
                                                      final CloudformationResource deployment) {
        return CloudformationResource.cloudformationResource(resourceId, "AWS::ApiGateway::Stage", Map.of(
                "RestApiId", api.reference(),
                "DeploymentId", deployment.reference(),
                "StageName", "stage"
        ));
    }
}
