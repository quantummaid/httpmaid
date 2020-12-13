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

package de.quantummaid.httpmaid.tests.specs.websockets;

import de.quantummaid.httpmaid.tests.givenwhenthen.TestEnvironment;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;

import java.util.List;
import java.util.Map;

import static de.quantummaid.httpmaid.HttpMaid.anHttpMaid;
import static de.quantummaid.httpmaid.awslambdacognitoauthorizer.CognitoConfigurators.toAuthorizeWebsocketsWithCognito;
import static de.quantummaid.httpmaid.awslambdacognitoauthorizer.CognitoConfigurators.toStoreCognitoDataInWebsocketContext;
import static de.quantummaid.httpmaid.tests.deployers.fakeawslambda.cognito.FakeCognitoClient.fakeCognitoClient;
import static de.quantummaid.httpmaid.tests.deployers.fakeawslambda.cognito.FakeTokenCreator.createToken;
import static de.quantummaid.httpmaid.tests.givenwhenthen.TestEnvironments.WEBSOCKET_ENVIRONMENTS;
import static de.quantummaid.httpmaid.websockets.WebsocketConfigurators.toAuthorizeWebsocketsUsing;
import static de.quantummaid.httpmaid.websockets.authorization.AuthorizationDecision.fail;

public final class AuthorizationSpecs {

    @ParameterizedTest
    @MethodSource(WEBSOCKET_ENVIRONMENTS)
    public void websocketsAccessQueryParameters(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .configured(toAuthorizeWebsocketsUsing(request -> fail()))
                        .build()
        )
                .when().aWebsocketIsTriedToBeConnected()
                .andWhen().theRuntimeDataIsQueried()
                .theQueriedNumberOfWebsocketsIs(0)
                .allWebsocketsHaveBeenClosed();
    }

    @ParameterizedTest
    @MethodSource(WEBSOCKET_ENVIRONMENTS)
    public void websocketsCanBeAuthorizedWithCognito(final TestEnvironment testEnvironment) {
        final CognitoIdentityProviderClient client = fakeCognitoClient("foo");
        testEnvironment.given(
                anHttpMaid()
                        .websocket((request, response) -> {
                            final String username = (String) request.additionalData().get("username");
                            response.setBody(username);
                        })
                        .configured(toAuthorizeWebsocketsWithCognito(
                                client,
                                "a",
                                "b",
                                request -> request.queryParameters().parameter("token")
                                )
                        )
                        .configured(toStoreCognitoDataInWebsocketContext((request, getUserResponse, authorizationToken) -> {
                            final String username = getUserResponse.username();
                            return Map.of("username", username);
                        }))
                        .build()
        )
                .when().aWebsocketIsConnected(Map.of("token", List.of(createToken("a", "b"))), Map.of())
                .andWhen().aWebsocketMessageIsSent("_")
                .oneWebsocketHasReceivedTheMessage("foo");
    }
}
