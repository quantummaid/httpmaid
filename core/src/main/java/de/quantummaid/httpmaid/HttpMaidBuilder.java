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
import de.quantummaid.httpmaid.chains.ChainRegistryBuilder;
import de.quantummaid.httpmaid.chains.Configurator;
import de.quantummaid.httpmaid.generator.GenerationCondition;
import de.quantummaid.httpmaid.generator.builder.ConditionStage;
import de.quantummaid.httpmaid.handler.http.HttpHandler;
import de.quantummaid.httpmaid.startupchecks.StartupChecks;
import de.quantummaid.httpmaid.websockets.broadcast.BroadcasterFactory;
import de.quantummaid.httpmaid.websockets.broadcast.Broadcasters;
import de.quantummaid.httpmaid.websockets.disconnect.DisconnectorFactory;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static de.quantummaid.httpmaid.HttpMaid.STARTUP_TIME;
import static de.quantummaid.httpmaid.startupchecks.StartupChecks.STARTUP_CHECKS;
import static de.quantummaid.httpmaid.util.Validators.validateNotNull;
import static de.quantummaid.httpmaid.websockets.WebsocketCatchAllRoute.webSocketCatchAllRoute;
import static de.quantummaid.httpmaid.websockets.WebsocketRoute.webSocketCategory;
import static de.quantummaid.httpmaid.websockets.WebsocketsModule.websocketsModule;
import static de.quantummaid.httpmaid.websockets.broadcast.Broadcasters.BROADCASTERS;
import static de.quantummaid.httpmaid.websockets.broadcast.Broadcasters.broadcasters;
import static de.quantummaid.httpmaid.websockets.sender.WebsocketSenders.WEBSOCKET_SENDERS;
import static de.quantummaid.httpmaid.websockets.sender.WebsocketSenders.websocketSenders;
import static java.time.Duration.between;
import static java.util.Arrays.asList;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class HttpMaidBuilder implements HttpConfiguration<HttpMaidBuilder> {
    private boolean autodetectionOfModules = true;
    private boolean performStartupChecks = true;
    private final CoreModule coreModule;
    private final List<Configurator> configurators;
    private final Broadcasters broadcasters = broadcasters();

    static HttpMaidBuilder httpMaidBuilder() {
        return new HttpMaidBuilder(CoreModule.coreModule(), new ArrayList<>());
    }

    public HttpMaidBuilder disableAutodectectionOfModules() {
        autodetectionOfModules = false;
        return this;
    }

    public HttpMaidBuilder disableStartupChecks() {
        performStartupChecks = false;
        return this;
    }

    @Override
    public ConditionStage<HttpMaidBuilder> serving(final Object handler, final PerRouteConfigurator... perRouteConfigurators) {
        validateNotNull(handler, "handler");
        return condition -> {
            coreModule.registerHandler(condition, handler, asList(perRouteConfigurators));
            return this;
        };
    }

    public HttpMaidBuilder websocket(final HttpHandler handler) {
        return websocket((Object) handler);
    }

    public HttpMaidBuilder websocket(final Object handler) {
        final GenerationCondition condition = webSocketCatchAllRoute();
        return serving(handler).when(condition);
    }

    public HttpMaidBuilder websocket(final String id, final HttpHandler handler) {
        return websocket(id, (Object) handler);
    }

    public HttpMaidBuilder websocket(final String id, final Object handler) {
        validateNotNull(id, "id");
        final GenerationCondition condition = webSocketCategory(id);
        return serving(handler).when(condition);
    }

    public <T, U> HttpMaidBuilder broadcastToWebsocketsUsing(final Class<T> broadcaster,
                                                             final Class<U> messageType,
                                                             final BroadcasterFactory<T, U> factory) {
        this.broadcasters.addBroadcaster(broadcaster, messageType, factory);
        return this;
    }

    public <T> HttpMaidBuilder disconnectWebsocketsUsing(final Class<T> disconnector,
                                                         final DisconnectorFactory<T> factory) {
        this.broadcasters.addDisconnector(disconnector, factory);
        return this;
    }

    @Override
    public HttpMaidBuilder configured(final Configurator configurator) {
        validateNotNull(configurator, "configurator");
        configurators.add(configurator);
        return this;
    }

    public HttpMaid build() {
        final Instant begin = Instant.now();
        final ChainRegistryBuilder chainRegistryBuilder = ChainRegistryBuilder.chainRegistryBuilder();
        chainRegistryBuilder.addMetaDatum(BROADCASTERS, broadcasters);
        chainRegistryBuilder.addMetaDatum(WEBSOCKET_SENDERS, websocketSenders());
        chainRegistryBuilder.addModule(coreModule);
        chainRegistryBuilder.addModule(websocketsModule());
        if (autodetectionOfModules) {
            chainRegistryBuilder.addModuleIfPresent("de.quantummaid.httpmaid.events.EventModule");
            chainRegistryBuilder.addModuleIfPresent("de.quantummaid.httpmaid.usecases.UseCasesModule");
            chainRegistryBuilder.addModuleIfPresent("de.quantummaid.httpmaid.mapmaid.MapMaidModule");
        }
        configurators.forEach(chainRegistryBuilder::addConfigurator);
        final ChainRegistry chainRegistry = chainRegistryBuilder.build();
        final HttpMaid httpMaid = HttpMaid.httpMaid(chainRegistry);
        final Instant end = Instant.now();
        final Duration startUpTime = between(begin, end);
        if (performStartupChecks) {
            final StartupChecks startupChecks = chainRegistry.getMetaDatum(STARTUP_CHECKS);
            startupChecks.check();
        }
        chainRegistry.addMetaDatum(STARTUP_TIME, startUpTime);
        return httpMaid;
    }
}

