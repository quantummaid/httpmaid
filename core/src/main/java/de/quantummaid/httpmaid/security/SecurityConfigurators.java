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

package de.quantummaid.httpmaid.security;

import de.quantummaid.httpmaid.handler.http.HttpRequest;
import de.quantummaid.httpmaid.security.authentication.Authenticator;
import de.quantummaid.httpmaid.security.authentication.AuthenticatorConfigurator;
import de.quantummaid.httpmaid.security.authorization.AuthorizerConfigurator;
import de.quantummaid.httpmaid.security.authorization.HttpAuthorizer;
import de.quantummaid.httpmaid.security.basicauth.BasicAuthAuthenticator;
import de.quantummaid.httpmaid.security.basicauth.BasicAuthConfigurator;
import de.quantummaid.httpmaid.security.filtering.FilterConfigurator;

import static de.quantummaid.httpmaid.handler.http.HttpRequest.httpRequest;
import static de.quantummaid.httpmaid.security.authentication.AuthenticatorConfigurator.authenticatorConfigurator;
import static de.quantummaid.httpmaid.security.authorization.AuthorizerConfigurator.authorizerConfigurator;
import static de.quantummaid.httpmaid.security.filtering.FilterConfigurator.filterConfigurator;
import static de.quantummaid.httpmaid.security.oauth2.OAuth2Authenticator.oAuth2Authenticator;
import static de.quantummaid.httpmaid.util.Validators.validateNotNull;
import static de.quantummaid.httpmaid.util.Validators.validateNotNullNorEmpty;

public final class SecurityConfigurators {

    private SecurityConfigurators() {
    }

    public static BasicAuthConfigurator toDoBasicAuthWith(final BasicAuthAuthenticator authenticator) {
        return BasicAuthConfigurator.basicAuthenticationConfigurator(authenticator);
    }

    public static AuthenticatorConfigurator toAuthenticateRequestsUsing(final Authenticator<HttpRequest> authenticator) {
        return authenticatorConfigurator(metaData -> {
            final HttpRequest request = httpRequest(metaData);
            return authenticator.authenticate(request);
        });
    }

    public static AuthenticatorConfigurator toAuthenticateUsingOAuth2BearerToken(final Authenticator<String> authenticator) {
        return toAuthenticateRequestsUsing(oAuth2Authenticator(authenticator));
    }

    public static AuthenticatorConfigurator toAuthenticateUsingCookie(final String cookieName,
                                                                      final Authenticator<String> authenticator) {
        validateNotNullNorEmpty(cookieName, "cookieName");
        validateNotNull(authenticator, "authenticator"); // NOSONAR
        return toAuthenticateRequestsUsing(request -> request.cookies()
                .getOptionalCookie(cookieName)
                .flatMap(authenticator::authenticate));
    }

    public static AuthenticatorConfigurator toAuthenticateUsingHeader(final String headerName,
                                                                      final Authenticator<String> authenticator) {
        validateNotNullNorEmpty(headerName, "headerName");
        validateNotNull(authenticator, "authenticator"); // NOSONAR
        return toAuthenticateRequestsUsing(request -> request.headers()
                .optionalHeader(headerName)
                .flatMap(authenticator::authenticate));
    }

    public static AuthenticatorConfigurator toAuthenticateUsingQueryParameter(final String parameterName,
                                                                              final Authenticator<String> authenticator) {
        validateNotNullNorEmpty(parameterName, "parameterName");
        validateNotNull(authenticator, "authenticator"); // NOSONAR
        return toAuthenticateRequestsUsing(request -> request.queryParameters()
                .getOptionalQueryParameter(parameterName)
                .flatMap(authenticator::authenticate));
    }

    public static AuthenticatorConfigurator toAuthenticateUsingPathParameter(final String parameterName,
                                                                             final Authenticator<String> authenticator) {
        validateNotNullNorEmpty(parameterName, "parameterName");
        validateNotNull(authenticator, "authenticator"); // NOSONAR
        return toAuthenticateRequestsUsing(request -> request.pathParameters()
                .getOptionalPathParameter(parameterName)
                .flatMap(authenticator::authenticate)).afterBodyProcessing();
    }

    public static AuthorizerConfigurator toAuthorizeRequestsUsing(final HttpAuthorizer authorizer) {
        return authorizerConfigurator(authorizer);
    }

    public static AuthorizerConfigurator toAuthorizeAllAuthenticatedRequests() {
        return toAuthorizeRequestsUsing((authenticationInformation, request) -> authenticationInformation.isPresent());
    }

    public static FilterConfigurator toFilterRequestsThat(final Filter filter) {
        return filterConfigurator(filter);
    }
}
