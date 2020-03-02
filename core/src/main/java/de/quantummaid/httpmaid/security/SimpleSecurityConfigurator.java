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

import de.quantummaid.httpmaid.chains.ChainName;
import de.quantummaid.httpmaid.chains.DependencyRegistry;
import de.quantummaid.httpmaid.chains.Processor;
import de.quantummaid.httpmaid.security.config.SecurityConfigurator;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.LinkedList;
import java.util.List;

import static de.quantummaid.httpmaid.security.SecurityProcessor.securityProcessor;
import static de.quantummaid.httpmaid.util.Validators.validateNotNull;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class SimpleSecurityConfigurator implements SecurityConfigurator<SimpleSecurityConfigurator> {
    private final Processor processor;
    private volatile ChainName phase;
    private final List<Filter> filters = new LinkedList<>();

    public static SimpleSecurityConfigurator simpleSecurityConfigurator(final Processor processor) {
        validateNotNull(processor, "processor");
        final SimpleSecurityConfigurator simpleSecurityConfigurator = new SimpleSecurityConfigurator(processor);
        simpleSecurityConfigurator.beforeBodyProcessing();
        return simpleSecurityConfigurator;
    }

    public SimpleSecurityConfigurator inPhase(final ChainName phase) {
        validateNotNull(phase, "phase");
        this.phase = phase;
        return this;
    }

    public SimpleSecurityConfigurator onlyRequestsThat(final Filter filter) {
        validateNotNull(filter, "filter");
        filters.add(filter);
        return this;
    }

    @Override
    public void configure(final DependencyRegistry dependencyRegistry) {
        final SecurityModule securityModule = dependencyRegistry.getDependency(SecurityModule.class);
        securityModule.addSecurityProcessor(securityProcessor(processor, phase, filters));
    }
}
