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

public final class Cognito {

    private Cognito() {
    }

    public static CloudformationResource pool(final CloudformationName resourceId) {
        return CloudformationResource.cloudformationResource(resourceId, "AWS::Cognito::UserPool", Map.of(
                "UserPoolName", resourceId.asId(),
                "Policies", Map.of(
                        "PasswordPolicy", Map.of(
                                "MinimumLength", 6,
                                "RequireLowercase", false,
                                "RequireNumbers", false,
                                "RequireSymbols", false,
                                "RequireUppercase", false
                        )
                )
        ));
    }

    public static CloudformationResource poolClient(final CloudformationName resourceId,
                                                    final CloudformationResource pool) {
        return CloudformationResource.cloudformationResource(resourceId, "AWS::Cognito::UserPoolClient", Map.of(
            "UserPoolId", pool.reference(),
                "ClientName", resourceId.asId(),
                "ExplicitAuthFlows", List.of("ALLOW_ADMIN_USER_PASSWORD_AUTH", "ALLOW_REFRESH_TOKEN_AUTH"),
                "ReadAttributes", List.of("email"),
                "CallbackURLs", List.of("https://example.org/"),
                "LogoutURLs", List.of("https://example.org/"),
                "AllowedOAuthFlowsUserPoolClient", true,
                "AllowedOAuthFlows", List.of("code"),
                "AllowedOAuthScopes", List.of("email", "openid", "aws.cognito.signin.user.admin"),
                "SupportedIdentityProviders", List.of("COGNITO")
        ));
    }
}
