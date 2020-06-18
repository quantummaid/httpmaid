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

package de.quantummaid.httpmaid.security.oauth2;

import de.quantummaid.httpmaid.handler.http.HttpRequest;
import de.quantummaid.httpmaid.security.authentication.Authenticator;
import de.quantummaid.httpmaid.security.authorization.AuthorizationHeader;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Optional;

import static de.quantummaid.httpmaid.http.Http.Headers.AUTHORIZATION;
import static de.quantummaid.httpmaid.util.Validators.validateNotNull;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class OAuth2Authenticator implements Authenticator<HttpRequest> {
    private final Authenticator<String> tokenAuthenticator;

    public static OAuth2Authenticator oAuth2Authenticator(final Authenticator<String> tokenAuthenticator) {
        validateNotNull(tokenAuthenticator, "tokenAuthenticator");
        return new OAuth2Authenticator(tokenAuthenticator);
    }

    @Override
    public Optional<Object> authenticate(final HttpRequest request) {
        return request.headers().optionalHeader(AUTHORIZATION)
                .flatMap(AuthorizationHeader::parse)
                .filter(authorizationHeader -> authorizationHeader.type().equals("Bearer"))
                .map(AuthorizationHeader::credentials)
                .flatMap(tokenAuthenticator::authenticate);
    }
}
