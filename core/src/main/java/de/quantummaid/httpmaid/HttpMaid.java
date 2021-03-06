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
import de.quantummaid.httpmaid.http.headers.ContentType;
import de.quantummaid.httpmaid.marshalling.Marshaller;
import de.quantummaid.httpmaid.marshalling.Marshallers;
import de.quantummaid.httpmaid.serialization.Serializer;
import de.quantummaid.httpmaid.websockets.broadcast.Broadcasters;
import de.quantummaid.httpmaid.websockets.broadcast.NonSerializingSender;
import de.quantummaid.httpmaid.websockets.broadcast.SerializingSender;
import de.quantummaid.httpmaid.websockets.disconnect.Disconnector;
import de.quantummaid.httpmaid.websockets.registry.WebsocketRegistry;
import de.quantummaid.httpmaid.websockets.sender.WebsocketSender;
import de.quantummaid.httpmaid.websockets.sender.WebsocketSenderId;
import de.quantummaid.httpmaid.websockets.sender.WebsocketSenders;
import de.quantummaid.reflectmaid.GenericType;
import de.quantummaid.reflectmaid.ReflectMaid;
import de.quantummaid.reflectmaid.resolvedtype.ResolvedType;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Optional;

import static de.quantummaid.httpmaid.CoreModule.REFLECT_MAID;
import static de.quantummaid.httpmaid.HttpMaidBuilder.httpMaidBuilder;
import static de.quantummaid.httpmaid.RuntimeInformation.runtimeInformation;
import static de.quantummaid.httpmaid.chains.MetaData.emptyMetaData;
import static de.quantummaid.httpmaid.chains.MetaDataKey.metaDataKey;
import static de.quantummaid.httpmaid.closing.ClosingActions.CLOSING_ACTIONS;
import static de.quantummaid.httpmaid.endpoint.RawResponse.rawResponse;
import static de.quantummaid.httpmaid.endpoint.SynchronizationWrapper.synchronizationWrapper;
import static de.quantummaid.httpmaid.http.headers.ContentType.json;
import static de.quantummaid.httpmaid.marshalling.Marshallers.MARSHALLERS;
import static de.quantummaid.httpmaid.serialization.Serializer.SERIALIZER;
import static de.quantummaid.httpmaid.util.Validators.validateNotNull;
import static de.quantummaid.httpmaid.websockets.WebsocketMetaDataKeys.WEBSOCKET_REGISTRY;
import static de.quantummaid.httpmaid.websockets.broadcast.Broadcasters.BROADCASTERS;
import static de.quantummaid.httpmaid.websockets.broadcast.NonSerializingSender.nonSerializingSender;
import static de.quantummaid.httpmaid.websockets.broadcast.SerializingSender.serializingSender;
import static de.quantummaid.httpmaid.websockets.disconnect.Disconnector.disconnector;
import static de.quantummaid.httpmaid.websockets.sender.WebsocketSenders.WEBSOCKET_SENDERS;
import static de.quantummaid.reflectmaid.GenericType.genericType;

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
        final RawRequest rawHttpRequest;
        try {
            rawHttpRequest = rawRequestExtractor.extract();
        } catch (final Exception e) {
            LOGGER.error("Exception in endpoint request handling", e);
            return;
            // throwing an exception here might pose a security risk (http://cwe.mitre.org/data/definitions/600.html)
        }
        final MetaData metaData = emptyMetaData();
        rawHttpRequest.enter(metaData);
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

    public void addWebsocketSender(final WebsocketSenderId websocketSenderId,
                                   final WebsocketSender<?> websocketSender) {
        final WebsocketSenders websocketSenders = getMetaDatum(WEBSOCKET_SENDERS);
        websocketSenders.addWebsocketSender(websocketSenderId, websocketSender);
    }

    public <T> Optional<T> getOptionalMetaDatum(final MetaDataKey<T> key) {
        validateNotNull(key, "key");
        return chainRegistry.getOptionalMetaDatum(key);
    }

    public <T> void setMetaDatum(final MetaDataKey<T> key, final T value) {
        chainRegistry.addMetaDatum(key, value);
    }

    public <T> T getMetaDatum(final MetaDataKey<T> key) {
        validateNotNull(key, "key");
        return chainRegistry.getMetaDatum(key);
    }

    public String dumpChains() {
        return chainRegistry.dump();
    }

    public RuntimeInformation queryRuntimeInformation() {
        final WebsocketRegistry websocketRegistry = chainRegistry.getMetaDatum(WEBSOCKET_REGISTRY).get();
        final long numberOfConnectedWebsockets = websocketRegistry.countConnections();
        return runtimeInformation(numberOfConnectedWebsockets);
    }

    public NonSerializingSender websocketSender() {
        final WebsocketRegistry websocketRegistry = getMetaDatum(WEBSOCKET_REGISTRY).get();
        final WebsocketSenders websocketSenders = getMetaDatum(WEBSOCKET_SENDERS);
        return nonSerializingSender(websocketRegistry, websocketSenders, emptyMetaData());
    }

    public <T> SerializingSender<T> websocketSender(final Class<T> messageType) {
        return websocketSender(messageType, json());
    }

    public <T> SerializingSender<T> websocketSender(final Class<T> messageType,
                                                    final ContentType contentType) {
        final GenericType<T> genericType = genericType(messageType);
        return websocketSender(genericType, contentType);
    }

    public <T> SerializingSender<T> websocketSender(final GenericType<T> messageType,
                                                    final ContentType contentType) {
        final Marshallers marshallers = getMetaDatum(MARSHALLERS);
        final Marshaller marshaller = marshallers.marshallerFor(contentType);
        final WebsocketRegistry websocketRegistry = getMetaDatum(WEBSOCKET_REGISTRY).get();
        final WebsocketSenders websocketSenders = getMetaDatum(WEBSOCKET_SENDERS);
        final Serializer serializer = getMetaDatum(SERIALIZER);
        final ReflectMaid reflectMaid = getMetaDatum(REFLECT_MAID);
        final ResolvedType resolvedMessageType = reflectMaid.resolve(messageType);
        return serializingSender(
                websocketRegistry,
                websocketSenders,
                resolvedMessageType,
                marshaller,
                serializer,
                emptyMetaData()
        );
    }

    public <T> T websocketBroadcaster(final Class<T> broadcasterType) {
        return websocketBroadcaster(broadcasterType, json());
    }

    public <T> T websocketBroadcaster(final Class<T> broadcasterType,
                                      final ContentType contentType) {
        final GenericType<T> genericType = genericType(broadcasterType);
        return websocketBroadcaster(genericType, contentType);
    }

    public <T> T websocketBroadcaster(final GenericType<T> broadcasterType,
                                      final ContentType contentType) {
        final Marshallers marshallers = getMetaDatum(MARSHALLERS);
        final Marshaller marshaller = marshallers.marshallerFor(contentType);
        final WebsocketRegistry websocketRegistry = getMetaDatum(WEBSOCKET_REGISTRY).get();
        final WebsocketSenders websocketSenders = getMetaDatum(WEBSOCKET_SENDERS);
        final Serializer serializer = getMetaDatum(SERIALIZER);
        final Broadcasters broadcasters = getMetaDatum(BROADCASTERS);
        return broadcasters.instantiateBroadcaster(
                broadcasterType,
                websocketRegistry,
                websocketSenders,
                serializer,
                marshaller,
                emptyMetaData()
        );
    }

    public Disconnector websocketDisconnector() {
        final WebsocketRegistry websocketRegistry = getMetaDatum(WEBSOCKET_REGISTRY).get();
        final WebsocketSenders websocketSenders = getMetaDatum(WEBSOCKET_SENDERS);
        return disconnector(websocketRegistry, websocketSenders, emptyMetaData());
    }

    public <T> T websocketDisconnector(final Class<T> disconnectorType) {
        final GenericType<T> genericType = genericType(disconnectorType);
        return websocketDisconnector(genericType);
    }

    public <T> T websocketDisconnector(final GenericType<T> disconnectorType) {
        final WebsocketRegistry websocketRegistry = getMetaDatum(WEBSOCKET_REGISTRY).get();
        final WebsocketSenders websocketSenders = getMetaDatum(WEBSOCKET_SENDERS);
        final Broadcasters broadcasters = getMetaDatum(BROADCASTERS);
        return broadcasters.instantiateDisconnector(
                disconnectorType,
                websocketRegistry,
                websocketSenders,
                emptyMetaData()
        );
    }

    public static HttpMaidBuilder anHttpMaid() {
        final ReflectMaid reflectMaid = ReflectMaid.aReflectMaid();
        return anHttpMaid(reflectMaid);
    }

    public static HttpMaidBuilder anHttpMaid(final ReflectMaid reflectMaid) {
        return httpMaidBuilder(reflectMaid);
    }

    @Override
    public void close() {
        final ClosingActions closingActions = chainRegistry.getMetaDatum(CLOSING_ACTIONS);
        try {
            closingActions.closeAll();
        } catch (final Exception e) {
            throw HttpMaidException.httpMaidException("exception during closing of HttpMaid", e);
        }
    }

    public ReflectMaid reflectMaid() {
        return chainRegistry.getMetaDatum(REFLECT_MAID);
    }
}
