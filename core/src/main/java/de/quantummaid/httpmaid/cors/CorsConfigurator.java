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

package de.quantummaid.httpmaid.cors;

import de.quantummaid.httpmaid.chains.ChainModule;
import de.quantummaid.httpmaid.chains.Configurator;
import de.quantummaid.httpmaid.chains.DependencyRegistry;
import de.quantummaid.httpmaid.cors.domain.ExposedHeader;
import de.quantummaid.httpmaid.cors.domain.ExposedHeaders;
import de.quantummaid.httpmaid.cors.domain.MaxAge;
import de.quantummaid.httpmaid.cors.domain.RequestedHeader;
import de.quantummaid.httpmaid.cors.policy.AllowedHeaders;
import de.quantummaid.httpmaid.cors.policy.AllowedMethods;
import de.quantummaid.httpmaid.cors.policy.AllowedOrigins;
import de.quantummaid.httpmaid.cors.policy.ResourceSharingPolicy;
import de.quantummaid.httpmaid.http.HttpRequestMethod;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static de.quantummaid.httpmaid.cors.CorsModule.corsModule;
import static de.quantummaid.httpmaid.cors.domain.ExposedHeaders.exposedHeaders;
import static de.quantummaid.httpmaid.cors.domain.MaxAge.maxAgeInSeconds;
import static de.quantummaid.httpmaid.cors.domain.MaxAge.undefinedMaxAge;
import static de.quantummaid.httpmaid.cors.policy.ResourceSharingPolicy.resourceSharingPolicy;
import static de.quantummaid.httpmaid.http.HttpRequestMethod.GET;
import static de.quantummaid.httpmaid.http.HttpRequestMethod.POST;
import static de.quantummaid.httpmaid.util.Validators.validateNotNull;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class CorsConfigurator implements Configurator {
    private final AllowedOrigins allowedOrigins;
    private volatile AllowedMethods allowedMethods;
    private volatile AllowedHeaders allowedHeaders;
    private volatile ExposedHeaders exposedHeaders;
    private volatile boolean credentialsSupport = false;
    private volatile MaxAge maxAge = undefinedMaxAge();

    public static CorsConfigurator corsConfigurator(final AllowedOrigins allowedOrigins) {
        validateNotNull(allowedOrigins, "allowedOrigins");
        final CorsConfigurator corsConfigurator = new CorsConfigurator(allowedOrigins);
        return corsConfigurator
                .withAllowedMethods(GET, POST)
                .withAllowedHeaders()
                .exposingTheResponseHeaders();
    }

    public CorsConfigurator withAllowedMethods(final HttpRequestMethod... methods) {
        validateNotNull(methods, "methods");
        final List<HttpRequestMethod> methodList = asList(methods);
        allowedMethods = requestedMethod -> methodList.parallelStream().anyMatch(requestedMethod::matches);
        return this;
    }

    public CorsConfigurator allowingAllHeaders() {
        this.allowedHeaders = requestedHeader -> true;
        return this;
    }

    public CorsConfigurator withAllowedHeaders(final String... headers) {
        validateNotNull(headers, "headers");
        final List<RequestedHeader> allowedHeaders = Arrays.stream(headers)
                .map(RequestedHeader::requestedHeader)
                .collect(toList());
        this.allowedHeaders = allowedHeaders::contains;
        return this;
    }

    public CorsConfigurator exposingTheResponseHeaders(final String... exposedHeaders) {
        this.exposedHeaders = exposedHeaders(stream(exposedHeaders)
                .map(ExposedHeader::exposedHeader)
                .collect(toList()));
        return this;
    }

    public CorsConfigurator exposingAllResponseHeaders() {
        return exposingTheResponseHeaders("*");
    }

    public CorsConfigurator allowingCredentials() {
        credentialsSupport = true;
        return this;
    }

    public CorsConfigurator notAllowingCredentials() {
        credentialsSupport = false;
        return this;
    }

    public CorsConfigurator withTimeOutAfter(final int timeout, final TimeUnit timeUnit) {
        final long seconds = timeUnit.toSeconds(timeout);
        maxAge = maxAgeInSeconds(seconds);
        return this;
    }

    @Override
    public List<ChainModule> supplyModulesIfNotAlreadyPresent() {
        return singletonList(corsModule());
    }

    @Override
    public void configure(final DependencyRegistry dependencyRegistry) {
        final CorsModule corsModule = dependencyRegistry.getDependency(CorsModule.class);
        final ResourceSharingPolicy resourceSharingPolicy = resourceSharingPolicy(
                allowedOrigins, allowedMethods, allowedHeaders, exposedHeaders, credentialsSupport, maxAge);
        corsModule.setResourceSharingPolicy(resourceSharingPolicy);
    }
}
