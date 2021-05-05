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

package de.quantummaid.httpmaid.testlambda;

import de.quantummaid.httpmaid.HttpMaid;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;

import java.util.Map;

import static de.quantummaid.graalvmlambdaruntime.GraalVmLambdaRuntime.startGraalVmLambdaRuntime;
import static de.quantummaid.httpmaid.awslambdacognitoauthorizer.CognitoConfigurators.toStoreCognitoDataInWebsocketContext;
import static de.quantummaid.httpmaid.awslambdacognitoauthorizer.CognitoWebsocketAuthorizer.cognitoWebsocketAuthorizer;
import static de.quantummaid.httpmaid.lambdastructure.Structures.LAMBDA_EVENT;
import static de.quantummaid.httpmaid.websockets.WebsocketConfigurators.toAuthorizeWebsocketsUsing;

@ToString
@EqualsAndHashCode
@Slf4j
public final class CognitoAuthorizerGraalVmLambda {
    private static final HttpMaid HTTP_MAID = Router.httpMaidForRouter(builder -> builder
            .websocket("returnLambdaContext", (request, response) -> {
                final String foo = (String) request.additionalData().get("foo");
                response.setBody(foo);
            })
            .configured(toAuthorizeWebsocketsUsing(() -> {
                final String region = System.getenv("REGION");
                final String poolId = System.getenv("POOL_ID");
                final String issuerUrl = String.format("https://cognito-idp.%s.amazonaws.com/%s", region, poolId);
                final String poolClientId = System.getenv("POOL_CLIENT_ID");
                final CognitoIdentityProviderClient client = CognitoIdentityProviderClient.create();
                return cognitoWebsocketAuthorizer(
                        client,
                        request -> request.queryParameters().parameter("access_token"),
                        issuerUrl,
                        poolClientId
                );
            }))
            .configured(toStoreCognitoDataInWebsocketContext(
                    (request, getUserResponse, authorizationToken) -> Map.of("foo", "bar"))));
    private final Router router = Router.router(HTTP_MAID);

    public Map<String, Object> handleRequest(final Map<String, Object> event) {
        log.debug("new lambda event: {}", event);
        LAMBDA_EVENT.runValidation(event);
        return router.route(event);
    }

    @SuppressWarnings("unchecked")
    public static void start() {
        final CognitoAuthorizerGraalVmLambda lambda = new CognitoAuthorizerGraalVmLambda();
        startGraalVmLambdaRuntime(map -> lambda.handleRequest((Map<String, Object>) map));
    }
}
