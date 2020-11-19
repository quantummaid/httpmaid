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

import de.quantummaid.httpmaid.awslambdacognitoauthorizer.policy.PolicyDocument;
import de.quantummaid.httpmaid.awslambdacognitoauthorizer.policy.PolicyEffect;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

import static de.quantummaid.httpmaid.awslambdacognitoauthorizer.policy.Policy.policy;
import static de.quantummaid.httpmaid.awslambdacognitoauthorizer.policy.PolicyDocument.policyDocument;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class AuthorizationDecision {
    private final boolean authorized;
    private final String principalId;
    private final Map<String, Object> additionalContext;

    public static AuthorizationDecision success(final String principalId) {
        return authorizationDecision(true, principalId, Map.of());
    }

    public static AuthorizationDecision success(final String principalId,
                                                final Map<String, Object> additionalContext) {
        return authorizationDecision(true, principalId, additionalContext);
    }

    public static AuthorizationDecision fail() {
        return fail("");
    }

    public static AuthorizationDecision fail(final String principalId) {
        return authorizationDecision(false, principalId, Map.of());
    }

    public static AuthorizationDecision authorizationDecision(final boolean authorized,
                                                              final String principalId,
                                                              final Map<String, Object> context) {
        return new AuthorizationDecision(authorized, principalId, context);
    }

    public Map<String, Object> asMap(final String methodArn, final Map<String, Object> context) {
        final PolicyEffect policyEffect = PolicyEffect.policyEffect(authorized);
        final PolicyDocument policyDocument = policyDocument(policy(policyEffect, "execute-api:Invoke", methodArn));
        final Map<String, Object> mergedContext = new HashMap<>();
        mergedContext.putAll(context);
        mergedContext.putAll(additionalContext);
        return Map.of(
                "principalId", principalId,
                "context", mergedContext,
                "policyDocument", policyDocument.asMap()
        );
    }
}
