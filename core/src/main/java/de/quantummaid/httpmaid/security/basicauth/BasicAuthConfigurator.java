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

package de.quantummaid.httpmaid.security.basicauth;

import de.quantummaid.httpmaid.chains.ChainName;
import de.quantummaid.httpmaid.chains.DependencyRegistry;
import de.quantummaid.httpmaid.handler.http.HttpRequest;
import de.quantummaid.httpmaid.security.Filter;
import de.quantummaid.httpmaid.security.authentication.AuthenticatorConfigurator;
import de.quantummaid.httpmaid.security.config.SecurityConfigurator;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import static de.quantummaid.httpmaid.handler.http.HttpRequest.httpRequest;
import static de.quantummaid.httpmaid.http.Http.Headers.WWW_AUTHENTICATE;
import static de.quantummaid.httpmaid.http.Http.StatusCodes.UNAUTHORIZED;
import static de.quantummaid.httpmaid.security.authentication.AuthenticatorConfigurator.authenticatorConfigurator;
import static de.quantummaid.httpmaid.security.basicauth.BasicAuthAuthentication.basicAuthAuthentication;
import static de.quantummaid.httpmaid.util.Validators.validateNotNull;
import static java.lang.String.format;
import static java.util.Objects.nonNull;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class BasicAuthConfigurator implements SecurityConfigurator<BasicAuthConfigurator> {
    private final AuthenticatorConfigurator authenticatorConfigurator;
    private volatile String realm;

    public static BasicAuthConfigurator basicAuthenticationConfigurator(final BasicAuthAuthenticator authenticator) {
        final BasicAuthAuthentication basicAuthAuthentication = basicAuthAuthentication(authenticator);
        final AuthenticatorConfigurator authenticatorConfigurator = authenticatorConfigurator(metaData -> {
            final HttpRequest request = httpRequest(metaData);
            return basicAuthAuthentication.authenticate(request);
        });
        return new BasicAuthConfigurator(authenticatorConfigurator);
    }

    @Override
    public BasicAuthConfigurator inPhase(final ChainName phase) {
        authenticatorConfigurator.inPhase(phase);
        return this;
    }

    @Override
    public BasicAuthConfigurator onlyRequestsThat(final Filter filter) {
        authenticatorConfigurator.onlyRequestsThat(filter);
        return this;
    }

    public BasicAuthConfigurator withMessage(final String message) {
        validateNotNull(message, "message");
        realm = message;
        return this;
    }

    @Override
    public void configure(final DependencyRegistry dependencyRegistry) {
        authenticatorConfigurator.rejectingUnauthenticatedRequestsUsing((request, response) -> {
            final StringBuilder headerBuilder = new StringBuilder();
            headerBuilder.append("Basic");
            if (nonNull(realm)) {
                headerBuilder.append(format(" realm=\"%s\"", realm));
            }
            response.addHeader(WWW_AUTHENTICATE, headerBuilder.toString());
            response.setStatus(UNAUTHORIZED);
        });
        authenticatorConfigurator.configure(dependencyRegistry);
    }
}
