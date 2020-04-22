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

import de.quantummaid.httpmaid.chains.ChainExtender;
import de.quantummaid.httpmaid.chains.ChainModule;
import de.quantummaid.httpmaid.chains.ChainName;
import de.quantummaid.httpmaid.util.Validators;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.LinkedList;
import java.util.List;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class SecurityModule implements ChainModule {
    private final List<SecurityProcessor> securityProcessors = new LinkedList<>();

    public static ChainModule securityModule() {
        return new SecurityModule();
    }

    public void addSecurityProcessor(final SecurityProcessor securityProcessor) {
        Validators.validateNotNull(securityProcessor, "securityProcessor");
        securityProcessors.add(securityProcessor);
    }

    @Override
    public void register(final ChainExtender extender) {
        securityProcessors.forEach(securityProcessor -> {
            final ChainName chainName = securityProcessor.chainName();
            extender.appendProcessor(chainName, securityProcessor);
        });
    }
}
