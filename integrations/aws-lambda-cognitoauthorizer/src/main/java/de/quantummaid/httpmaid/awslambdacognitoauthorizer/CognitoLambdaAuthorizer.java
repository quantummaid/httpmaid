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

import de.quantummaid.httpmaid.awslambda.AwsLambdaEvent;
import de.quantummaid.httpmaid.awslambdacognitoauthorizer.policy.PolicyDocument;
import de.quantummaid.httpmaid.awslambdacognitoauthorizer.policy.PolicyEffect;
import de.quantummaid.httpmaid.chains.MetaData;
import de.quantummaid.httpmaid.http.HeadersBuilder;
import de.quantummaid.httpmaid.http.QueryParameters;
import de.quantummaid.httpmaid.http.QueryParametersBuilder;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.quantummaid.httpmaid.HttpMaidChainKeys.QUERY_PARAMETERS;
import static de.quantummaid.httpmaid.HttpMaidChainKeys.REQUEST_HEADERS;
import static de.quantummaid.httpmaid.awslambda.AwsLambdaEvent.awsLambdaEvent;
import static de.quantummaid.httpmaid.awslambdacognitoauthorizer.policy.Policy.policy;
import static de.quantummaid.httpmaid.awslambdacognitoauthorizer.policy.PolicyDocument.policyDocument;
import static de.quantummaid.httpmaid.awslambdacognitoauthorizer.policy.PolicyEffect.ALLOW;
import static de.quantummaid.httpmaid.awslambdacognitoauthorizer.policy.PolicyEffect.DENY;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class CognitoLambdaAuthorizer implements AutoCloseable {
    private final CognitoIdentityProviderClient client;
    private final TokenExtractor tokenExtractor;

    public static CognitoLambdaAuthorizer cognitoLambdaAuthorizer(final TokenExtractor tokenExtractor) {
        final CognitoIdentityProviderClient client = CognitoIdentityProviderClient.create();
        return cognitoLambdaAuthorizer(client, tokenExtractor);
    }

    public static CognitoLambdaAuthorizer cognitoLambdaAuthorizer(final CognitoIdentityProviderClient client,
                                                                  final TokenExtractor tokenExtractor) {
        return new CognitoLambdaAuthorizer(client, tokenExtractor);
    }

    public Map<String, Object> delegate(final Map<String, Object> event) {
        final AwsLambdaEvent awsLambdaEvent = awsLambdaEvent(event);
        final MetaData metaData = extractMetaData(awsLambdaEvent);
        final String accessToken = tokenExtractor.extract(metaData);
        try {
            final GetUserResponse getUserResponse = client.getUser(builder -> builder.accessToken(accessToken));
            final AttributeType subjectAttribute = getUserResponse.userAttributes().get(0);
            final String subject = subjectAttribute.value();
            final PolicyDocument policyDocument = createPolicyDocument(ALLOW, awsLambdaEvent);
            final String username = getUserResponse.username();
            return Map.of(
                    "principalId", subject,
                    "context", Map.of("username", username),
                    "policyDocument", policyDocument.asMap()
            );
        } catch (final NotAuthorizedException | PasswordResetRequiredException | UserNotConfirmedException | UserNotFoundException e) {
            final PolicyDocument policyDocument = createPolicyDocument(DENY, awsLambdaEvent);
            return Map.of("policyDocument", policyDocument.asMap());
        }
    }

    private PolicyDocument createPolicyDocument(final PolicyEffect effect,
                                                final AwsLambdaEvent awsLambdaEvent) {
        final String methodArn = awsLambdaEvent.getAsString("methodArn");
        return policyDocument(policy(effect, "execute-api:Invoke", methodArn));
    }

    private MetaData extractMetaData(final AwsLambdaEvent event) {
        final MetaData metaData = MetaData.emptyMetaData();
        final QueryParametersBuilder queryParametersBuilder = QueryParameters.builder();
        final Map<String, List<String>> queryParameters = event.getOrDefault("multiValueQueryStringParameters", HashMap::new);
        queryParameters.forEach(queryParametersBuilder::withParameter);
        metaData.set(QUERY_PARAMETERS, queryParametersBuilder.build());
        final Map<String, List<String>> headers = event.getOrDefault("multiValueHeaders", HashMap::new);
        final HeadersBuilder headersBuilder = HeadersBuilder.headersBuilder();
        headersBuilder.withHeadersMap(headers);
        metaData.set(REQUEST_HEADERS, headersBuilder.build());
        return metaData;
    }

    @Override
    public void close() {
        client.close();
    }
}
