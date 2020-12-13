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

package de.quantummaid.httpmaid.websockets.authorization;

import de.quantummaid.httpmaid.chains.MetaDataKey;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.ToString;

import java.util.LinkedHashMap;
import java.util.Map;

import static de.quantummaid.httpmaid.chains.MetaDataKey.metaDataKey;

@ToString
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class AuthorizationDecision {
    public static final MetaDataKey<AuthorizationDecision> AUTHORIZATION_DECISION = metaDataKey("AUTHORIZATION_DECISION");

    private final boolean authorized;
    private Map<String, Object> additionalData;

    public static AuthorizationDecision success() {
        return success(Map.of());
    }

    public static AuthorizationDecision success(final Map<String, Object> additionalData) {
        return authorizationDecision(true, additionalData);
    }

    public static AuthorizationDecision fail() {
        return authorizationDecision(false, Map.of());
    }

    public static AuthorizationDecision authorizationDecision(final boolean authorized,
                                                              final Map<String, Object> context) {
        return new AuthorizationDecision(authorized, context);
    }

    public boolean isAuthorized() {
        return authorized;
    }

    public void mergeInAdditionalData(final Map<String, Object> additionalData) {
        final Map<String, Object> newData = new LinkedHashMap<>(this.additionalData);
        newData.putAll(additionalData);
        this.additionalData = newData;
    }

    public Map<String, Object> additionalData() {
        return additionalData;
    }
}
