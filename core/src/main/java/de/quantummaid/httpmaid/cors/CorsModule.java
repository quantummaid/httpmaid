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

import de.quantummaid.httpmaid.chains.ChainExtender;
import de.quantummaid.httpmaid.chains.ChainModule;
import de.quantummaid.httpmaid.chains.ChainName;
import de.quantummaid.httpmaid.cors.policy.ResourceSharingPolicy;
import de.quantummaid.httpmaid.chains.rules.Jump;
import de.quantummaid.httpmaid.http.HttpRequestMethod;
import de.quantummaid.httpmaid.util.Validators;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import static de.quantummaid.httpmaid.HttpMaidChainKeys.METHOD;
import static de.quantummaid.httpmaid.HttpMaidChains.*;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class CorsModule implements ChainModule {
    private static final ChainName CORS_CHAIN = ChainName.chainName("CORS");
    private ResourceSharingPolicy resourceSharingPolicy;

    public static ChainModule corsModule() {
        return new CorsModule();
    }

    public void setResourceSharingPolicy(final ResourceSharingPolicy resourceSharingPolicy) {
        Validators.validateNotNull(resourceSharingPolicy, "resourceSharingPolicy");
        this.resourceSharingPolicy = resourceSharingPolicy;
    }

    @Override
    public void register(final ChainExtender extender) {
        extender.createChain(CORS_CHAIN, Jump.jumpTo(POST_PROCESS), Jump.jumpTo(EXCEPTION_OCCURRED));
        extender.appendProcessor(CORS_CHAIN, PreflightRequestProcessor.preflightRequestProcessor(resourceSharingPolicy));
        extender.routeIfEquals(PRE_PROCESS, Jump.jumpTo(CORS_CHAIN), METHOD, HttpRequestMethod.OPTIONS);
        extender.appendProcessor(PREPARE_RESPONSE, SimpleCrossOriginRequestProcessor.simpleCrossOriginRequestProcessor(resourceSharingPolicy));
    }
}
