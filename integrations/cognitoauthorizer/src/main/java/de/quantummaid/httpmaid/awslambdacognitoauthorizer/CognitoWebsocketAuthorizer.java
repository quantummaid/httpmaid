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
import de.quantummaid.httpmaid.chains.MetaData;
import de.quantummaid.httpmaid.chains.MetaDataKey;
import de.quantummaid.httpmaid.handler.http.HttpRequest;
import de.quantummaid.httpmaid.websockets.authorization.AuthorizationDecision;
import de.quantummaid.httpmaid.websockets.authorization.WebsocketAuthorizer;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

import java.util.Map;

import static de.quantummaid.httpmaid.awslambdacognitoauthorizer.jwt.JwtParser.extractJwtPayload;
import static de.quantummaid.httpmaid.chains.MetaDataKey.metaDataKey;
import static de.quantummaid.httpmaid.websockets.authorization.AuthorizationDecision.fail;
import static de.quantummaid.httpmaid.websockets.authorization.AuthorizationDecision.success;
import static de.quantummaid.reflectmaid.validators.NotNullValidator.validateNotNull;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class CognitoWebsocketAuthorizer implements WebsocketAuthorizer {
    public static final MetaDataKey<GetUserResponse> GET_USER_RESPONSE = metaDataKey("GET_USER_RESPONSE");
    public static final MetaDataKey<Map<String, Object>> AUTHORIZATION_TOKEN = metaDataKey("AUTHORIZATION_TOKEN");

    private final CognitoIdentityProviderClient client;
    private final TokenExtractor tokenExtractor;
    private final String issuerUrl;
    private final String clientId;

    public static CognitoWebsocketAuthorizer cognitoWebsocketAuthorizer(final CognitoIdentityProviderClient client,
                                                                        final TokenExtractor tokenExtractor,
                                                                        final String issuerUrl,
                                                                        final String clientId) {
        validateNotNull(client, "client");
        validateNotNull(tokenExtractor, "tokenExtractor");
        validateNotNull(issuerUrl, "issuerUrl");
        validateNotNull(clientId, "clientId");
        return new CognitoWebsocketAuthorizer(client, tokenExtractor, issuerUrl, clientId);
    }

    @Override
    public AuthorizationDecision isAuthorized(final HttpRequest request) {
        final String accessToken = tokenExtractor.extract(request);
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
            final GetUserResponse getUserResponse = client.getUser(builder -> builder
                    .accessToken(accessToken)
            );
            log.debug("call to cognito was successful: {}", getUserResponse);
            final MetaData metaData = request.getMetaData();
            metaData.set(GET_USER_RESPONSE, getUserResponse);
            metaData.set(AUTHORIZATION_TOKEN, jwtInformation.payloadMap());
            return success();
        } catch (final NotAuthorizedException | PasswordResetRequiredException | UserNotConfirmedException | UserNotFoundException e) {
            log.debug("rejecting authorization request because call to cognito threw an exception", e);
            return fail();
        }
    }
}
