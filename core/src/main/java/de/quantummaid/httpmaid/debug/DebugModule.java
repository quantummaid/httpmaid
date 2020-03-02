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

package de.quantummaid.httpmaid.debug;

import de.quantummaid.httpmaid.chains.ChainExtender;
import de.quantummaid.httpmaid.chains.ChainModule;
import de.quantummaid.httpmaid.chains.ChainName;
import de.quantummaid.httpmaid.chains.ChainRegistry;
import de.quantummaid.httpmaid.path.PathTemplate;
import de.quantummaid.httpmaid.chains.rules.Drop;
import de.quantummaid.httpmaid.chains.rules.Jump;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import static de.quantummaid.httpmaid.HttpMaidChainKeys.PATH;
import static de.quantummaid.httpmaid.HttpMaidChainKeys.RESPONSE_BODY_STRING;
import java.util.HashMap;

import static de.quantummaid.httpmaid.HttpMaidChainKeys.*;
import static de.quantummaid.httpmaid.HttpMaidChains.POST_PROCESS;
import static de.quantummaid.httpmaid.HttpMaidChains.PRE_PROCESS;
import static de.quantummaid.httpmaid.http.Http.StatusCodes.OK;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class DebugModule implements ChainModule {
    private static final ChainName DEBUG_CHAIN = ChainName.chainName("DEBUG");
    private static final PathTemplate PATH_TEMPLATE = PathTemplate.pathTemplate("/internals");

    public static ChainModule debugModule() {
        return new DebugModule();
    }

    @Override
    public void register(final ChainExtender extender) {
        final ChainRegistry registry = extender.getMetaDatum(ChainRegistry.CHAIN_REGISTRY);
        extender.createChain(DEBUG_CHAIN, Jump.jumpTo(POST_PROCESS), Drop.drop());
        extender.appendProcessor(DEBUG_CHAIN, metaData -> {
            final String dump = registry.dump();
            metaData.set(RESPONSE_BODY_STRING, dump);
            metaData.set(RESPONSE_STATUS, OK);
            metaData.set(RESPONSE_HEADERS, new HashMap<>());
        });
        extender.routeIf(PRE_PROCESS, Jump.jumpTo(DEBUG_CHAIN), PATH, PATH_TEMPLATE::matches, PATH_TEMPLATE.toString());
    }
}
