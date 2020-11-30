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

package de.quantummaid.httpmaid.awslambdacognitoauthorizer.jwt;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Map;

@ToString
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class JwtInformation {
    private final Map<String, Object> payloadMap;

    public static JwtInformation jwtInformation(final Map<String, Object> payloadMap) {
        return new JwtInformation(payloadMap);
    }

    public boolean matches(final String expectedIssuerUrl,
                           final String expectedClientId) {
        final String issuerUrl = issuerUrl();
        if (!issuerUrl.equals(expectedIssuerUrl)) {
            return false;
        }
        final String clientId = clientId();
        return clientId.equals(expectedClientId);
    }

    public String issuerUrl() {
        return (String) payloadMap.get("iss");
    }

    public String clientId() {
        return (String) payloadMap.get("client_id");
    }

    public Map<String, Object> payloadMap() {
        return payloadMap;
    }
}
