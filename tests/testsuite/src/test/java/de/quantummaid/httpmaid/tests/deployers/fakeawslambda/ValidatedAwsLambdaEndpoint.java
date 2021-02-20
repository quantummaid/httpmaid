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

package de.quantummaid.httpmaid.tests.deployers.fakeawslambda;

import de.quantummaid.httpmaid.HttpMaid;
import de.quantummaid.httpmaid.awslambda.AwsLambdaEndpoint;
import de.quantummaid.httpmaid.awslambda.AwsWebsocketLambdaEndpoint;
import de.quantummaid.httpmaid.awslambda.authorizer.LambdaWebsocketAuthorizer;
import de.quantummaid.httpmaid.awslambda.sender.apigateway.ApiGatewayClientFactory;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.Map;

import static de.quantummaid.httpmaid.awslambda.AwsLambdaEndpoint.awsLambdaEndpointFor;
import static de.quantummaid.httpmaid.awslambda.AwsWebsocketLambdaEndpoint.awsWebsocketLambdaEndpointFor;
import static de.quantummaid.httpmaid.awslambda.EventUtils.isAuthorizationRequest;
import static de.quantummaid.httpmaid.awslambda.EventUtils.isWebSocketRequest;
import static de.quantummaid.httpmaid.awslambda.authorizer.LambdaWebsocketAuthorizer.lambdaWebsocketAuthorizer;
import static de.quantummaid.httpmaid.awslambda.sender.apigateway.async.ApiGatewayAsyncClientFactory.asyncApiGatewayClientFactory;
import static de.quantummaid.httpmaid.lambdastructure.Structures.LAMBDA_EVENT;
import static de.quantummaid.httpmaid.tests.deployers.fakeawslambda.FakeApiGatewayAsyncClientFactory.fakeApiGatewayClientFactory;
import static de.quantummaid.httpmaid.util.Validators.validateNotNull;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ValidatedAwsLambdaEndpoint {
    private final AwsLambdaEndpoint httpEndpoint;
    private final AwsWebsocketLambdaEndpoint websocketEndpoint;
    private final LambdaWebsocketAuthorizer authorizer;

    public static ValidatedAwsLambdaEndpoint validatedLambdaEndpointWithAuthorizer(final HttpMaid httpMaid,
                                                                                   final int apiGatewayManagementServerPort) {
        validateNotNull(httpMaid, "httpMaid");
        final LambdaWebsocketAuthorizer authorizer = lambdaWebsocketAuthorizer(httpMaid);
        return validatedLambdaEndpoint(httpMaid, authorizer, apiGatewayManagementServerPort);
    }

    public static ValidatedAwsLambdaEndpoint validatedLambdaEndpoint(final HttpMaid httpMaid,
                                                                     final int apiGatewayManagementServerPort) {
        return validatedLambdaEndpoint(httpMaid, null, apiGatewayManagementServerPort);
    }

    private static ValidatedAwsLambdaEndpoint validatedLambdaEndpoint(final HttpMaid httpMaid,
                                                                      final LambdaWebsocketAuthorizer authorizer,
                                                                      final int apiGatewayManagementServerPort) {
        validateNotNull(httpMaid, "httpMaid");
        final AwsLambdaEndpoint httpEndpoint = awsLambdaEndpointFor(httpMaid);
        final ApiGatewayClientFactory apiGatewayClientFactory = asyncApiGatewayClientFactory(
                fakeApiGatewayClientFactory(apiGatewayManagementServerPort));
        final AwsWebsocketLambdaEndpoint websocketEndpoint = awsWebsocketLambdaEndpointFor(
                httpMaid,
                "not-an-actual-region",
                apiGatewayClientFactory
        );
        return new ValidatedAwsLambdaEndpoint(httpEndpoint, websocketEndpoint, authorizer);
    }

    public boolean hasAuthorizer() {
        return authorizer != null;
    }

    public Map<String, Object> delegate(final Map<String, Object> event) {
        LAMBDA_EVENT.runValidation(event);
        if (isAuthorizationRequest(event)) {
            return authorizer.delegate(event);
        } else if (!isWebSocketRequest(event)) {
            return httpEndpoint.delegate(event);
        } else {
            return websocketEndpoint.delegate(event);
        }
    }
}
