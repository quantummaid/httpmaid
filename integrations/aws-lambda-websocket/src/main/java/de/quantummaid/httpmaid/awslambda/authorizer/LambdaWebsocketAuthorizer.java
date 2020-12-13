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

package de.quantummaid.httpmaid.awslambda.authorizer;

import de.quantummaid.httpmaid.HttpMaid;
import de.quantummaid.httpmaid.awslambda.AwsLambdaEvent;
import de.quantummaid.httpmaid.http.Headers;
import de.quantummaid.httpmaid.http.QueryParameters;
import de.quantummaid.httpmaid.websockets.authorization.AuthorizationDecision;
import de.quantummaid.httpmaid.websockets.endpoint.RawWebsocketAuthorizationBuilder;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.UUID;

import static de.quantummaid.httpmaid.awslambda.AwsLambdaEvent.AWS_LAMBDA_EVENT;
import static de.quantummaid.httpmaid.awslambda.AwsLambdaEvent.awsLambdaEvent;
import static de.quantummaid.httpmaid.awslambda.EventUtils.extractMethodArn;
import static de.quantummaid.httpmaid.awslambda.WebsocketEventUtils.extractHeaders;
import static de.quantummaid.httpmaid.awslambda.WebsocketEventUtils.extractQueryParameters;
import static de.quantummaid.httpmaid.awslambda.authorizer.AuthorizationDecisionMapper.mapAuthorizationDecision;
import static de.quantummaid.httpmaid.websockets.authorization.AuthorizationDecision.AUTHORIZATION_DECISION;
import static de.quantummaid.httpmaid.websockets.endpoint.RawWebsocketAuthorizationBuilder.rawWebsocketAuthorizationBuilder;
import static de.quantummaid.reflectmaid.validators.NotNullValidator.validateNotNull;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class LambdaWebsocketAuthorizer implements LambdaAuthorizer {
    public static final String ADDITIONAL_DATA_KEY = "additionalData";
    public static final String AUTHORIZER_EVENT_KEY = "event";

    private final HttpMaid httpMaid;

    public static LambdaWebsocketAuthorizer lambdaWebsocketAuthorizer(final HttpMaid httpMaid) {
        validateNotNull(httpMaid, "httpMaid");
        return new LambdaWebsocketAuthorizer(httpMaid);
    }

    @Override
    public Map<String, Object> delegate(final Map<String, Object> event) {
        final AwsLambdaEvent awsLambdaEvent = awsLambdaEvent(event);

        final String methodArn = extractMethodArn(awsLambdaEvent);
        log.debug("extracted methodArn: {}", methodArn);

        final AuthorizationDecision decision = authorize(awsLambdaEvent, httpMaid);
        final Map<String, Object> additionalData = decision.additionalData();

        final String serializedAdditionalData = MapSerializer.toString(additionalData);
        final String serializedEvent = MapSerializer.toString(event);
        final Map<String, Object> authorizerContext = Map.of(
                ADDITIONAL_DATA_KEY, serializedAdditionalData,
                AUTHORIZER_EVENT_KEY, serializedEvent
        );

        final String principalId = UUID.randomUUID().toString();
        return mapAuthorizationDecision(decision.isAuthorized(), methodArn, principalId, authorizerContext);
    }

    public static AuthorizationDecision authorize(final AwsLambdaEvent event,
                                                  final HttpMaid httpMaid) {
        return httpMaid.handleRequestSynchronously(() -> {
            final RawWebsocketAuthorizationBuilder builder = rawWebsocketAuthorizationBuilder();
            builder.withAdditionalMetaData(AWS_LAMBDA_EVENT, event);
            final QueryParameters queryParameters = extractQueryParameters(event);
            builder.withQueryParameters(queryParameters);
            final Headers headers = extractHeaders(event);
            builder.withHeaders(headers);
            return builder.build();
        }, response -> response.metaData().get(AUTHORIZATION_DECISION));
    }
}
