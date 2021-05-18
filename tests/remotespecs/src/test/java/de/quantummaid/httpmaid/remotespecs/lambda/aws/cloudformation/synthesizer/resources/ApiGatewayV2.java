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

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation.synthesizer.CloudformationResource.cloudformationResource;
import static de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation.synthesizer.IntrinsicFunctions.join;
import static de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation.synthesizer.IntrinsicFunctions.sub;
import static java.lang.String.format;

public final class ApiGatewayV2 {

    private ApiGatewayV2() {
    }

    public static CloudformationResource httpApi(final CloudformationName resourceId) {
        return cloudformationResource(resourceId, "AWS::ApiGatewayV2::Api", Map.of(
                "Name", resourceId.asId(),
                "ProtocolType", "HTTP"
        ));
    }

    public static CloudformationResource httpApiIntegration(final CloudformationName resourceId,
                                                            final String payloadVersion,
                                                            final CloudformationResource api,
                                                            final CloudformationResource function) {
        final Object integrationUri = buildInvocationUri(function);
        return cloudformationResource(resourceId, "AWS::ApiGatewayV2::Integration", List.of(api), Map.of(
                "ApiId", api.reference(),
                "IntegrationType", "AWS_PROXY",
                "PayloadFormatVersion", payloadVersion,
                "IntegrationUri", integrationUri
        ));
    }

    public static CloudformationResource defaultRoute(final CloudformationName resourceId,
                                                      final CloudformationResource api,
                                                      final CloudformationResource apiIntegration,
                                                      final CloudformationResource authorizer) {
        final Object target = join("/", "integrations", apiIntegration.reference());
        final Map<String, Object> map = new HashMap<>(Map.of(
                "ApiId", api.reference(),
                "RouteKey", "$default",
                "AuthorizationType", "NONE",
                "Target", target
        ));
        if (authorizer != null) {
            map.put("AuthorizationType", "JWT");
            map.put("AuthorizerId", authorizer.reference());
        } else {
            map.put("AuthorizationType", "NONE");
        }
        return cloudformationResource(resourceId, "AWS::ApiGatewayV2::Route", List.of(api, apiIntegration), map);
    }

    public static CloudformationResource websocketsApiConnectRoute(final CloudformationName resourceId,
                                                                   final CloudformationResource api,
                                                                   final CloudformationResource integration,
                                                                   final CloudformationResource authorizer) {
        return websocketsApiRoute(resourceId, api, "$connect", integration, authorizer);
    }

    public static CloudformationResource websocketsApiDisconnectRoute(final CloudformationName resourceId,
                                                                      final CloudformationResource api,
                                                                      final CloudformationResource integration) {
        return websocketsApiRoute(resourceId, api, "$disconnect", integration, null);
    }

    private static CloudformationResource websocketsApiRoute(final CloudformationName resourceId,
                                                             final CloudformationResource api,
                                                             final String routeKey,
                                                             final CloudformationResource integration,
                                                             final CloudformationResource authorizer) {
        final Object target = join("/", "integrations", integration.reference());
        final Map<String, Object> map = new LinkedHashMap<>(Map.of(
                "ApiId", api.reference(),
                "RouteKey", routeKey,
                "RouteResponseSelectionExpression", "$default",
                "Target", target
        ));
        if (authorizer != null) {
            map.put("AuthorizationType", "CUSTOM");
            map.put("AuthorizerId", authorizer.reference());
        } else {
            map.put("AuthorizationType", "NONE");
        }
        return cloudformationResource(resourceId, "AWS::ApiGatewayV2::Route", map);
    }

    public static CloudformationResource deployment(final CloudformationName resourceId,
                                                    final CloudformationResource api,
                                                    final CloudformationResource apiIntegration,
                                                    final CloudformationResource apiDefaultRoute) {
        return cloudformationResource(resourceId,
                "AWS::ApiGatewayV2::Deployment",
                List.of(api, apiIntegration, apiDefaultRoute),
                Map.of(
                        "ApiId", api.reference()
                ));
    }

    public static CloudformationResource stage(final CloudformationName resourceId,
                                               final CloudformationResource api,
                                               final CloudformationResource deployment) {
        return cloudformationResource(resourceId, "AWS::ApiGatewayV2::Stage", List.of(api, deployment), Map.of(
                "StageName", "$default",
                "ApiId", api.reference(),
                "DeploymentId", deployment.reference()
        ));
    }

    public static CloudformationResource jwtAuthorizer(final CloudformationName resourceId,
                                                       final CloudformationResource api,
                                                       final CloudformationResource pool,
                                                       final CloudformationResource poolClient) {
        return cloudformationResource(resourceId, "AWS::ApiGatewayV2::Authorizer", Map.of(
                "Name", resourceId.asId(),
                "ApiId", api.reference(),
                "AuthorizerType", "JWT",
                "IdentitySource", List.of("$request.header.Authorization"),
                "JwtConfiguration", Map.of(
                        "Issuer", pool.attribute("ProviderURL"),
                        "Audience", List.of(poolClient.reference())
                )
        ));
    }

    public static CloudformationResource authorizer(final CloudformationName resourceId,
                                                    final CloudformationResource api,
                                                    final CloudformationResource functionRole,
                                                    final CloudformationResource function) {
        final Object authorizerUri = sub(String.format("arn:aws:apigateway:${AWS::Region}:lambda:path/2015-03-31/functions/${%s.Arn}/invocations",
                function.name().asId()));
        return cloudformationResource(resourceId, "AWS::ApiGatewayV2::Authorizer", Map.of(
                "Name", resourceId.asId(),
                "ApiId", api.reference(),
                "AuthorizerCredentialsArn", functionRole.attribute("Arn"),
                "AuthorizerType", "REQUEST",
                "AuthorizerUri", authorizerUri,
                "IdentitySource", List.of()
        ));
    }

    public static CloudformationResource websocketsApi(final CloudformationName resourceId,
                                                       final CloudformationResource function) {
        final Object target = buildInvocationUri(function);
        return cloudformationResource(resourceId, "AWS::ApiGatewayV2::Api", Map.of(
                "Name", resourceId.asId(),
                "ProtocolType", "WEBSOCKET",
                "RouteSelectionExpression", "$request.body.action",
                "Target", target
        ));
    }

    public static CloudformationResource websocketsApiIntegration(final CloudformationName resourceId,
                                                                  final CloudformationResource api,
                                                                  final CloudformationResource function) {
        final Object integrationUri = buildInvocationUri(function);
        return cloudformationResource(resourceId, "AWS::ApiGatewayV2::Integration", Map.of(
                "ApiId", api.reference(),
                "IntegrationType", "AWS_PROXY",
                "IntegrationUri", integrationUri
        ));
    }

    public static CloudformationResource websocketsApiDefaultRoute(final CloudformationName resourceId,
                                                                   final CloudformationResource api,
                                                                   final CloudformationResource integration) {
        final Object target = join("/", "integrations", integration.reference());
        return cloudformationResource(resourceId, "AWS::ApiGatewayV2::Route", Map.of(
                "ApiId", api.reference(),
                "RouteKey", "$default",
                "AuthorizationType", "NONE",
                "RouteResponseSelectionExpression", "$default",
                "Target", target
        ));
    }

    public static CloudformationResource websocketsApiRouteResponse(final CloudformationName resourceId,
                                                                    final CloudformationResource api,
                                                                    final CloudformationResource route) {
        return cloudformationResource(resourceId, "AWS::ApiGatewayV2::RouteResponse", Map.of(
                "ApiId", api.reference(),
                "RouteId", route.reference(),
                "RouteResponseKey", "$default"
        ));
    }

    public static CloudformationResource websocketsApiDeployment(final CloudformationName resourceId,
                                                                 final CloudformationResource api,
                                                                 final CloudformationResource connectRoute,
                                                                 final CloudformationResource defaultRoute,
                                                                 final CloudformationResource defaultResponse) {
        return cloudformationResource(resourceId,
                "AWS::ApiGatewayV2::Deployment",
                List.of(connectRoute, defaultRoute, defaultResponse),
                Map.of(
                        "ApiId", api.reference()
                ));
    }

    public static CloudformationResource websocketsApiStage(final CloudformationName resourceId,
                                                            final CloudformationResource api,
                                                            final CloudformationResource deployment) {
        return cloudformationResource(resourceId, "AWS::ApiGatewayV2::Stage", Map.of(
                "StageName", "stage",
                "ApiId", api.reference(),
                "DeploymentId", deployment.reference()
        ));
    }

    public static Object buildInvocationUri(final CloudformationResource function) {
        return sub(format(
                "arn:aws:apigateway:${AWS::Region}:lambda:path/2015-03-31/functions/${%s.Arn}/invocations",
                function.name().asId()
                )
        );
    }
}
