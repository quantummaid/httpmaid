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

import de.quantummaid.httpmaid.awslambda.authorizer.policy.PolicyDocument;
import de.quantummaid.httpmaid.awslambda.authorizer.policy.PolicyEffect;

import java.util.Map;

import static de.quantummaid.httpmaid.awslambda.authorizer.policy.Policy.policy;
import static de.quantummaid.httpmaid.awslambda.authorizer.policy.PolicyDocument.policyDocument;
import static de.quantummaid.httpmaid.awslambda.authorizer.policy.PolicyEffect.policyEffect;

public final class AuthorizationDecisionMapper {

    private AuthorizationDecisionMapper() {
    }

    public static Map<String, Object> mapAuthorizationDecision(final boolean authorized,
                                                               final String methodArn,
                                                               final String principalId,
                                                               final Map<String, Object> context) {
        final PolicyEffect policyEffect = policyEffect(authorized);
        final PolicyDocument policyDocument = policyDocument(policy(policyEffect, "execute-api:Invoke", methodArn));
        return Map.of(
                "principalId", principalId,
                "context", context,
                "policyDocument", policyDocument.asMap()
        );
    }
}
