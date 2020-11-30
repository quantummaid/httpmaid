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

import de.quantummaid.httpmaid.awslambdacognitoauthorizer.jwt.JwtInformation;
import de.quantummaid.httpmaid.handler.http.HttpRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

import java.util.Map;

import static de.quantummaid.httpmaid.awslambda.AwsLambdaEvent.AWS_LAMBDA_EVENT;
import static de.quantummaid.httpmaid.awslambdacognitoauthorizer.AuthorizationDecision.fail;
import static de.quantummaid.httpmaid.awslambdacognitoauthorizer.AuthorizationDecision.success;
import static de.quantummaid.httpmaid.awslambdacognitoauthorizer.BasicLambdaAuthorizer.basicLambdaAuthorizer;
import static de.quantummaid.httpmaid.awslambdacognitoauthorizer.jwt.JwtParser.extractJwtPayload;
import static de.quantummaid.httpmaid.handler.http.HttpRequest.httpRequest;
import static de.quantummaid.mapmaid.shared.validators.NotNullValidator.validateNotNull;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class CognitoLambdaAuthorizer implements LambdaAuthorizer {
    private final CognitoIdentityProviderClient client;
    private final BasicLambdaAuthorizer basicLambdaAuthorizer;

    public static CognitoLambdaAuthorizer cognitoLambdaAuthorizer(final String poolId,
                                                                  final String region,
                                                                  final String clientId,
                                                                  final TokenExtractor tokenExtractor) {
        final String issuerUrl = String.format("https://cognito-idp.%s.amazonaws.com/%s", region, poolId);
        return cognitoLambdaAuthorizer(issuerUrl, clientId, tokenExtractor);
    }

    public static CognitoLambdaAuthorizer cognitoLambdaAuthorizer(final String issuerUrl,
                                                                  final String clientId,
                                                                  final TokenExtractor tokenExtractor) {
        final CognitoIdentityProviderClient client = CognitoIdentityProviderClient.create();
        return cognitoLambdaAuthorizer(client,
                issuerUrl,
                clientId,
                tokenExtractor,
                (request, event, getUserResponse, authorizationToken) -> Map.of()
        );
    }

    public static CognitoLambdaAuthorizer cognitoLambdaAuthorizer(final CognitoIdentityProviderClient client,
                                                                  final String issuerUrl,
                                                                  final String clientId,
                                                                  final TokenExtractor tokenExtractor,
                                                                  final ContextEnricher contextEnricher) {
        validateNotNull(client, "client");
        validateNotNull(issuerUrl, "issuerUrl");
        validateNotNull(tokenExtractor, "tokenExtractor");
        return new CognitoLambdaAuthorizer(client, basicLambdaAuthorizer(metaData -> {
            final HttpRequest httpRequest = httpRequest(metaData);
            final String accessToken = tokenExtractor.extract(httpRequest);
            try {
                final JwtInformation jwtInformation = extractJwtPayload(accessToken);
                if (!jwtInformation.matches(issuerUrl, clientId)) {
                    log.debug("rejecting token because issuer was '{}' and client id was '{}' but has to be '{}' and '{}'",
                            jwtInformation.issuerUrl(),
                            jwtInformation.clientId(),
                            issuerUrl,
                            clientId
                    );
                    return fail();
                }
                log.debug("calling cognito...");
                final GetUserResponse getUserResponse = client.getUser(builder -> builder.accessToken(accessToken));
                log.debug("call to cognito was successful: {}", getUserResponse);
                final AttributeType subjectAttribute = getUserResponse.userAttributes().get(0);
                final String subject = subjectAttribute.value();
                final Map<String, Object> context = contextEnricher.enrich(
                        httpRequest,
                        metaData.get(AWS_LAMBDA_EVENT),
                        getUserResponse,
                        jwtInformation.payloadMap()
                );
                return success(subject, context);
            } catch (final NotAuthorizedException | PasswordResetRequiredException | UserNotConfirmedException | UserNotFoundException e) {
                log.debug("rejecting authorization request because call to cognito threw an exception", e);
                return fail();
            }
        }));
    }

    @Override
    public Map<String, Object> delegate(final Map<String, Object> event) {
        return basicLambdaAuthorizer.delegate(event);
    }

    @Override
    public void close() {
        client.close();
    }
}
