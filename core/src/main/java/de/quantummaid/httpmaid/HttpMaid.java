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

package de.quantummaid.httpmaid;

import de.quantummaid.httpmaid.chains.*;
import de.quantummaid.httpmaid.closing.ClosingActions;
import de.quantummaid.httpmaid.purejavaendpoint.PureJavaEndpoint;
import de.quantummaid.httpmaid.util.Validators;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.Optional;

import static de.quantummaid.httpmaid.HttpMaidBuilder.httpMaidBuilder;

/**
 * A configured {@link HttpMaid} instance. Can be deployed using an endpoint like
 * {@link PureJavaEndpoint}
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class HttpMaid implements AutoCloseable {
    private final ChainRegistry chainRegistry;

    public static HttpMaid httpMaid(final ChainRegistry chainRegistry) {
        Validators.validateNotNull(chainRegistry, "chainRegistry");
        return new HttpMaid(chainRegistry);
    }

    public void handle(final ChainName chainName,
                       final MetaData metaData) {
        chainRegistry.putIntoChain(chainName, metaData, metaData1 -> {
        });
    }

    public void handleRequest(final MetaData metaData,
                              final FinalConsumer responseHandler) {
        chainRegistry.putIntoChain(HttpMaidChains.INIT, metaData, finalMetaData -> {
            try {
                responseHandler.consume(metaData);
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public <T> T getMetaDatum(final MetaDataKey<T> key) {
        Validators.validateNotNull(key, "key");
        return chainRegistry.getMetaDatum(key);
    }

    public <T> Optional<T> getOptionalMetaDatum(final MetaDataKey<T> key) {
        Validators.validateNotNull(key, "key");
        return chainRegistry.getOptionalMetaDatum(key);
    }

    public String dumpChains() {
        return chainRegistry.dump();
    }

    public static HttpMaidBuilder anHttpMaid() {
        return httpMaidBuilder();
    }

    @Override
    public void close() {
        final ClosingActions closingActions = chainRegistry.getMetaDatum(ClosingActions.CLOSING_ACTIONS);
        closingActions.closeAll();
    }
}
