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

package de.quantummaid.httpmaid.awslambdacognitoauthorizer.policy;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class PolicyDocument {
    private static final String VERSION = "2012-10-17";

    private final List<Policy> policies;

    public static PolicyDocument policyDocument(final Policy... policies) {
        return new PolicyDocument(asList(policies));
    }

    public Map<String, Object> asMap() {
        final List<Map<String, String>> statementList = policies.stream()
                .map(Policy::asMap)
                .collect(Collectors.toList());
        return Map.of(
                "Version", VERSION,
                "Statement", statementList
        );
    }
}
