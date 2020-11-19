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

package de.quantummaid.httpmaid.awslambdacognitoauthorizer;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

import java.util.Map;

import static de.quantummaid.httpmaid.awslambdacognitoauthorizer.AuthorizationDecision.fail;
import static de.quantummaid.httpmaid.awslambdacognitoauthorizer.AuthorizationDecision.success;
import static de.quantummaid.httpmaid.awslambdacognitoauthorizer.LambdaAuthorizer.lambdaAuthorizer;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class CognitoLambdaAuthorizer implements AutoCloseable {
    private final CognitoIdentityProviderClient client;
    private final LambdaAuthorizer lambdaAuthorizer;

    public static CognitoLambdaAuthorizer cognitoLambdaAuthorizer(final TokenExtractor tokenExtractor) {
        final CognitoIdentityProviderClient client = CognitoIdentityProviderClient.create();
        return cognitoLambdaAuthorizer(client, tokenExtractor);
    }

    public static CognitoLambdaAuthorizer cognitoLambdaAuthorizer(final CognitoIdentityProviderClient client,
                                                                  final TokenExtractor tokenExtractor) {
        return new CognitoLambdaAuthorizer(client, lambdaAuthorizer(metaData -> {
            final String accessToken = tokenExtractor.extract(metaData);
            try {
                final GetUserResponse getUserResponse = client.getUser(builder -> builder.accessToken(accessToken));
                final AttributeType subjectAttribute = getUserResponse.userAttributes().get(0);
                final String subject = subjectAttribute.value();
                final String username = getUserResponse.username();
                return success(subject, Map.of("username", username));
            } catch (final NotAuthorizedException | PasswordResetRequiredException | UserNotConfirmedException | UserNotFoundException e) {
                return fail();
            }
        }));
    }

    public Map<String, Object> delegate(final Map<String, Object> event) {
        return lambdaAuthorizer.delegate(event);
    }

    @Override
    public void close() {
        client.close();
    }
}
