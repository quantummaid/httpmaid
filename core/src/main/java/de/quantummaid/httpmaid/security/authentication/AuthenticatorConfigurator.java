/*
 * Copyright (c) 2019 Richard Hauswald - https://quantummaid.de/.
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

package de.quantummaid.httpmaid.security.authentication;

import de.quantummaid.httpmaid.CoreModule;
import de.quantummaid.httpmaid.chains.ChainName;
import de.quantummaid.httpmaid.chains.DependencyRegistry;
import de.quantummaid.httpmaid.chains.MetaData;
import de.quantummaid.httpmaid.handler.http.HttpHandler;
import de.quantummaid.httpmaid.security.Filter;
import de.quantummaid.httpmaid.security.SimpleSecurityConfigurator;
import de.quantummaid.httpmaid.security.config.SecurityConfigurator;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.LinkedList;
import java.util.List;

import static de.quantummaid.httpmaid.security.authentication.AuthenticatorProcessor.authenticatorProcessor;
import static de.quantummaid.httpmaid.util.Validators.validateNotNull;
import static java.util.Objects.nonNull;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class AuthenticatorConfigurator implements SecurityConfigurator<AuthenticatorConfigurator> {
    private final SimpleSecurityConfigurator simpleSecurityConfigurator;
    private final AuthenticatorId authenticatorId;
    private final List<Filter> optionalRequests;
    private volatile HttpHandler rejectionHandler;

    public static AuthenticatorConfigurator authenticatorConfigurator(final Authenticator<MetaData> authenticator) {
        validateNotNull(authenticator, "authenticator");
        final AuthenticatorId authenticatorId = AuthenticatorId.uniqueAuthenticatorId();
        final List<Filter> optionalRequests = new LinkedList<>();
        final AuthenticatorProcessor processor = authenticatorProcessor(authenticator, authenticatorId, optionalRequests);
        final SimpleSecurityConfigurator simpleSecurityConfigurator = SimpleSecurityConfigurator.simpleSecurityConfigurator(processor);
        return new AuthenticatorConfigurator(simpleSecurityConfigurator, authenticatorId, optionalRequests);
    }

    public AuthenticatorConfigurator inPhase(final ChainName phase) {
        simpleSecurityConfigurator.inPhase(phase);
        return this;
    }

    public AuthenticatorConfigurator onlyRequestsThat(final Filter filter) {
        simpleSecurityConfigurator.onlyRequestsThat(filter);
        return this;
    }

    public AuthenticatorConfigurator notFailingOnMissingAuthentication() {
        return notFailingOnMissingAuthenticationForRequestsThat(request -> true);
    }

    public AuthenticatorConfigurator notFailingOnMissingAuthenticationForRequestsThat(final Filter filter) {
        validateNotNull(filter, "filter");
        optionalRequests.add(filter);
        return this;
    }

    public AuthenticatorConfigurator notFailingOnMissingAuthenticationForRequestsTo(final String... paths) {
        return notFailingOnMissingAuthenticationForRequestsThat(Filter.pathsFilter(paths));
    }

    public AuthenticatorConfigurator failingOnMissingAuthenticationOnlyForRequestsThat(final Filter filter) {
        validateNotNull(filter, "filter");
        return notFailingOnMissingAuthenticationForRequestsThat(request -> !filter.filter(request));
    }

    public AuthenticatorConfigurator failingOnMissingAuthenticationOnlyForRequestsTo(final String... paths) {
        return notFailingOnMissingAuthenticationForRequestsThat(Filter.pathsFilter(paths));
    }

    public AuthenticatorConfigurator rejectingUnauthenticatedRequestsUsing(final HttpHandler rejectionHandler) {
        validateNotNull(rejectionHandler, "rejectionHandler");
        this.rejectionHandler = rejectionHandler;
        return this;
    }

    @Override
    public void configure(final DependencyRegistry dependencyRegistry) {
        simpleSecurityConfigurator.configure(dependencyRegistry);

        if (nonNull(rejectionHandler)) {
            final CoreModule coreModule = dependencyRegistry.getDependency(CoreModule.class);
            coreModule.addExceptionMapper(throwable -> {
                if (!(throwable instanceof CouldNotAuthenticateException)) {
                    return false;
                }
                return ((CouldNotAuthenticateException) throwable).authenticatorId().equals(authenticatorId);
            }, (exception, metaData) -> rejectionHandler.handle(metaData));
        }
    }
}
