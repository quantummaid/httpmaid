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

import de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation.synthesizer.CloudformationModule;
import de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation.synthesizer.CloudformationResource;
import de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation.synthesizer.CloudformationTemplateBuilder;
import de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation.synthesizer.Namespace;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import static de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation.synthesizer.CloudformationOutput.cloudformationOutput;
import static de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation.synthesizer.resources.ApiGatewayV2.*;
import static de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation.synthesizer.resources.Iam.functionPermission;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class WebsocketApiModule implements CloudformationModule {
    private final Namespace namespace;
    private final CloudformationResource functionRole;
    private final CloudformationResource function;
    private final boolean authorized;

    public static WebsocketApiModule websocketApiModule(final Namespace namespace,
                                                        final CloudformationResource functionRole,
                                                        final CloudformationResource function,
                                                        final boolean authorized) {
        return new WebsocketApiModule(namespace, functionRole, function, authorized);
    }

    @Override
    public void apply(final CloudformationTemplateBuilder builder) {
        final CloudformationResource websocketsApi = websocketsApi(
                namespace.id("WebsocketsApi"), namespace.id("WebsocketsApi"), function);
        final CloudformationResource websocketAuthorizer;
        if (authorized) {
            websocketAuthorizer = authorizer(
                    namespace.id("WsAuth"), namespace.id("WsAuth"), websocketsApi, functionRole, function);
            builder.withResources(websocketAuthorizer);
        } else {
            websocketAuthorizer = null;
        }
        final CloudformationResource websocketsApiConnectIntegration = websocketsApiIntegration(
                namespace.id("WebsocketsApiConnectIntegration"), websocketsApi, function);
        final CloudformationResource websocketsApiConnectRoute = websocketsApiConnectRoute(
                namespace.id("WebsocketsApiConnectRoute"), websocketsApi, websocketsApiConnectIntegration,
                websocketAuthorizer);
        final CloudformationResource websocketsApiDisconnectRoute = websocketsApiDisconnectRoute(
                namespace.id("WebsocketsApiDisconnectRoute"), websocketsApi, websocketsApiConnectIntegration);
        final CloudformationResource websocketsApiDefaultIntegration = websocketsApiIntegration(
                namespace.id("WebsocketsApiDefaultIntegration"), websocketsApi, function);
        final CloudformationResource websocketsApiDefaultRoute = websocketsApiDefaultRoute(
                namespace.id("WebsocketsApiDefaultRoute"), websocketsApi, websocketsApiDefaultIntegration);
        final CloudformationResource websocketsApiRouteResponse = websocketsApiRouteResponse(
                namespace.id("WebsocketsApiDefaultResponse"), websocketsApi, websocketsApiDefaultRoute);
        final CloudformationResource websocketsApiDeployment = websocketsApiDeployment(
                namespace.id("WebsocketsApiDeployment"), websocketsApi, websocketsApiConnectRoute,
                websocketsApiDefaultRoute, websocketsApiRouteResponse);
        final CloudformationResource websocketsApiStage = websocketsApiStage(
                namespace.id("WebsocketsApiStage"), websocketsApi, websocketsApiDeployment);
        if (authorized) {
            final CloudformationResource websocketsAuthorizerPermission = functionPermission(
                    namespace.id("WsAuthFnPerm"), function, websocketsApi, websocketsApiStage);
            builder.withResources(websocketsAuthorizerPermission);
        }
        builder.withResources(websocketsApi, websocketsApiConnectIntegration, websocketsApiDisconnectRoute,
                websocketsApiConnectRoute, websocketsApiDefaultIntegration, websocketsApiDefaultRoute,
                websocketsApiRouteResponse, websocketsApiDeployment, websocketsApiStage);
        builder.withOutputs(cloudformationOutput(namespace.id("WebsocketApiId"), websocketsApi.reference()));
    }
}
