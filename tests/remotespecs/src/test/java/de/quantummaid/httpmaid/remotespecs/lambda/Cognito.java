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

package de.quantummaid.httpmaid.remotespecs.lambda;

import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminInitiateAuthResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthFlowType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthenticationResultType;

import java.util.Map;

public final class Cognito {

    private Cognito() {
    }

    public static String generateValidAccessToken(final String poolId,
                                                  final String poolClientId,
                                                  final String username) {
        try (CognitoIdentityProviderClient client = CognitoIdentityProviderClient.create()) {
            signUp(client, poolClientId, username);
            confirmUser(client, poolId, username);
            return getToken(client, poolId, poolClientId, username);
        }
    }

    private static void signUp(final CognitoIdentityProviderClient client,
                               final String poolClientId,
                               final String username) {
        client.signUp(builder -> {
            builder.clientId(poolClientId);
            builder.username(username);
            builder.password(username);
        });
    }

    private static void confirmUser(final CognitoIdentityProviderClient client,
                                    final String poolId,
                                    final String username) {
        client.adminConfirmSignUp(builder -> {
            builder.username(username);
            builder.userPoolId(poolId);
        });
    }

    private static String getToken(final CognitoIdentityProviderClient client,
                                   final String poolId,
                                   final String poolClientId,
                                   final String username) {
        final AdminInitiateAuthResponse authResponse = client.adminInitiateAuth(builder -> {
            builder.userPoolId(poolId);
            builder.clientId(poolClientId);
            builder.authFlow(AuthFlowType.ADMIN_USER_PASSWORD_AUTH);
            builder.authParameters(Map.of(
                    "USERNAME", username,
                    "PASSWORD", username
            ));
        });
        final AuthenticationResultType authenticationResult = authResponse.authenticationResult();
        return authenticationResult.accessToken();
    }
}
