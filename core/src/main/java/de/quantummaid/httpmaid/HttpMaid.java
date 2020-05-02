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

import de.quantummaid.httpmaid.chains.ChainRegistry;
import de.quantummaid.httpmaid.chains.MetaData;
import de.quantummaid.httpmaid.chains.MetaDataKey;
import de.quantummaid.httpmaid.closing.ClosingActions;
import de.quantummaid.httpmaid.endpoint.*;
import de.quantummaid.httpmaid.websockets.endpoint.RawWebsocketMessage;
import de.quantummaid.httpmaid.websockets.registry.WebsocketRegistry;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Optional;

import static de.quantummaid.httpmaid.HttpMaidBuilder.httpMaidBuilder;
import static de.quantummaid.httpmaid.chains.MetaData.emptyMetaData;
import static de.quantummaid.httpmaid.chains.MetaDataKey.metaDataKey;
import static de.quantummaid.httpmaid.endpoint.RawResponse.rawResponse;
import static de.quantummaid.httpmaid.endpoint.SynchronizationWrapper.synchronizationWrapper;
import static de.quantummaid.httpmaid.util.Validators.validateNotNull;
import static de.quantummaid.httpmaid.websockets.WebsocketMetaDataKeys.WEBSOCKET_REGISTRY;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class HttpMaid implements AutoCloseable {
    public static final MetaDataKey<Duration> STARTUP_TIME = metaDataKey("STARTUP_TIME");
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpMaid.class);

    private final ChainRegistry chainRegistry;

    public static HttpMaid httpMaid(final ChainRegistry chainRegistry) {
        validateNotNull(chainRegistry, "chainRegistry");
        return new HttpMaid(chainRegistry);
    }

    public <T> T handleRequestSynchronously(final RawRequestExtractor<RawRequest> rawRequestExtractor,
                                            final RawResponseFactory<T> rawResponseFactory) {
        final SynchronizationWrapper<T> synchronizationWrapper = synchronizationWrapper();
        handleRequest(rawRequestExtractor, response -> {
            final T returnedResponse = rawResponseFactory.createResponse(response);
            synchronizationWrapper.setObject(returnedResponse);
        });
        return synchronizationWrapper.getObject();
    }

    public void handleRequest(final RawRequestExtractor<RawRequest> rawRequestExtractor,
                              final RawResponseHandler rawResponseHandler) {
        final RawRequest rawRequest;
        try {
            rawRequest = rawRequestExtractor.extract();
        } catch (final Exception e) {
            LOGGER.error("Exception in endpoint request handling", e);
            return;
            // throwing an exception here might pose a security risk (http://cwe.mitre.org/data/definitions/600.html)
        }
        final MetaData metaData = emptyMetaData();
        rawRequest.enter(metaData);
        chainRegistry.putIntoChain(HttpMaidChains.INIT, metaData, finalMetaData -> {
            final RawResponse rawResponse = rawResponse(finalMetaData);
            try {
                rawResponseHandler.handle(rawResponse);
            } catch (final Exception e) {
                LOGGER.error("Exception in endpoint reponse handling", e);
                // throwing an exception here might pose a security risk (http://cwe.mitre.org/data/definitions/600.html)
            }
        });
    }

    public void handleWebsocketConnect() {
        System.out.println("Connect websocket!");
    }

    public void handleWebsocketMessage(final RawRequestExtractor<RawWebsocketMessage> rawRequestExtractor) {
        final RawWebsocketMessage rawWebsocketMessage;
        try {
            rawWebsocketMessage = rawRequestExtractor.extract();
        } catch (final Exception e) {
            LOGGER.error("Exception in endpoint request handling", e);
            return;
            // throwing an exception here might pose a security risk (http://cwe.mitre.org/data/definitions/600.html)
        }
        final MetaData metaData = emptyMetaData();
        rawWebsocketMessage.enter(metaData);
        chainRegistry.putIntoChain(HttpMaidChains.INIT, metaData, finalMetaData -> {
        });
    }

    public void handleWebsocketDisconnect() {
        System.out.println("Disconnect websocket!");
    }

    public void setWebsocketRegistry(final WebsocketRegistry websocketRegistry) {
        validateNotNull(websocketRegistry, "websocketRegistry");
        chainRegistry.addMetaDatum(WEBSOCKET_REGISTRY, websocketRegistry);
    }

    public <T> T getMetaDatum(final MetaDataKey<T> key) {
        validateNotNull(key, "key");
        return chainRegistry.getMetaDatum(key);
    }

    public <T> Optional<T> getOptionalMetaDatum(final MetaDataKey<T> key) {
        validateNotNull(key, "key");
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
