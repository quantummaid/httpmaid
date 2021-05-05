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

package de.quantummaid.httpmaid.websockets;

import de.quantummaid.httpmaid.chains.ChainExtender;
import de.quantummaid.httpmaid.chains.ChainModule;
import de.quantummaid.httpmaid.chains.ChainName;
import de.quantummaid.httpmaid.runtimeconfiguration.RuntimeConfiguration;
import de.quantummaid.httpmaid.runtimeconfiguration.RuntimeConfigurationValue;
import de.quantummaid.httpmaid.runtimeconfiguration.RuntimeConfigurationValueProvider;
import de.quantummaid.httpmaid.websockets.additionaldata.AdditionalWebsocketDataProvider;
import de.quantummaid.httpmaid.websockets.authorization.WebsocketAuthorizer;
import de.quantummaid.httpmaid.websockets.registry.InMemoryRegistry;
import de.quantummaid.httpmaid.websockets.registry.WebsocketRegistry;
import de.quantummaid.httpmaid.websockets.registry.filter.header.HeaderFilter;
import de.quantummaid.httpmaid.websockets.registry.filter.queryparameter.QueryParameterFilter;
import de.quantummaid.httpmaid.websockets.sender.WebsocketSenders;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Map;

import static de.quantummaid.httpmaid.CoreModule.RUNTIME_CONFIGURATION;
import static de.quantummaid.httpmaid.HttpMaidChains.*;
import static de.quantummaid.httpmaid.chains.ChainName.chainName;
import static de.quantummaid.httpmaid.chains.rules.Consume.consume;
import static de.quantummaid.httpmaid.chains.rules.Drop.drop;
import static de.quantummaid.httpmaid.chains.rules.Jump.jumpTo;
import static de.quantummaid.httpmaid.websockets.WebsocketMetaDataKeys.*;
import static de.quantummaid.httpmaid.websockets.authorization.AuthorizationDecision.success;
import static de.quantummaid.httpmaid.websockets.processors.AddAdditionalWebsocketDataProcessor.addAdditionalWebsocketDataProcessor;
import static de.quantummaid.httpmaid.websockets.processors.AddWebsocketsMetaDataProcessor.addWebsocketRegistryProcessor;
import static de.quantummaid.httpmaid.websockets.processors.AuthorizeWebsocketProcessor.authorizeWebsocketProcessor;
import static de.quantummaid.httpmaid.websockets.processors.CreateWebsocketRegistryEntryProcessor.createWebsocketRegistryEntryProcessor;
import static de.quantummaid.httpmaid.websockets.processors.DetermineWebsocketRouteProcessor.determineWebsocketRouteProcessor;
import static de.quantummaid.httpmaid.websockets.processors.PutWebsocketInRegistryProcessor.putWebsocketInRegistryProcessor;
import static de.quantummaid.httpmaid.websockets.processors.RemoveWebsocketFromRegistryProcessor.removeWebsocketFromRegistryProcessor;
import static de.quantummaid.httpmaid.websockets.processors.RestoreWebsocketContextInformationProcessor.restoreWebsocketContextInformationProcessor;
import static de.quantummaid.httpmaid.websockets.registry.filter.header.AllowListHeaderFilter.onlyAllowingDefaultHeaders;
import static de.quantummaid.httpmaid.websockets.registry.filter.queryparameter.AllowAllQueryParameterFilter.allowAllQueryParameterFilter;
import static de.quantummaid.httpmaid.websockets.sender.WebsocketSenders.WEBSOCKET_SENDERS;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class WebsocketsModule implements ChainModule {
    private static final ChainName AUTHORIZE_WEBSOCKET = chainName("AUTHORIZE_WEBSOCKET");
    private static final ChainName CONNECT_WEBSOCKET = chainName("CONNECT_WEBSOCKET");
    private static final ChainName DISCONNECT_WEBSOCKET = chainName("DISCONNECT_WEBSOCKET");

    private String routeSelectionExpression = "message";
    private RuntimeConfigurationValueProvider<WebsocketRegistry> websocketRegistryProvider = InMemoryRegistry::inMemoryRegistry;
    private AdditionalWebsocketDataProvider additionalWebsocketDataProvider = request -> Map.of();
    private RuntimeConfigurationValueProvider<WebsocketAuthorizer> websocketAuthorizerProvider = () -> request -> success();
    private HeaderFilter headerFilter = onlyAllowingDefaultHeaders();
    private QueryParameterFilter queryParameterFilter = allowAllQueryParameterFilter();

    public static WebsocketsModule websocketsModule() {
        return new WebsocketsModule();
    }

    public void setRouteSelectionExpression(final String routeSelectionExpression) {
        this.routeSelectionExpression = routeSelectionExpression;
    }

    public void setWebsocketRegistryProvider(final RuntimeConfigurationValueProvider<WebsocketRegistry> websocketRegistryProvider) {
        this.websocketRegistryProvider = websocketRegistryProvider;
    }

    public void setAdditionalWebsocketDataProvider(final AdditionalWebsocketDataProvider additionalWebsocketDataProvider) {
        this.additionalWebsocketDataProvider = additionalWebsocketDataProvider;
    }

    public void setWebsocketAuthorizerProvider(final RuntimeConfigurationValueProvider<WebsocketAuthorizer> websocketAuthorizerProvider) {
        this.websocketAuthorizerProvider = websocketAuthorizerProvider;
    }

    public void setHeaderFilter(final HeaderFilter headerFilter) {
        this.headerFilter = headerFilter;
    }

    public void setQueryParameterFilter(final QueryParameterFilter queryParameterFilter) {
        this.queryParameterFilter = queryParameterFilter;
    }

    @Override
    public void register(final ChainExtender extender) {
        final RuntimeConfiguration runtimeConfiguration = extender.getMetaDatum(RUNTIME_CONFIGURATION);
        final RuntimeConfigurationValue<WebsocketRegistry> websocketRegistry = runtimeConfiguration.create(websocketRegistryProvider);

        extender.addMetaDatum(WEBSOCKET_REGISTRY, websocketRegistry);

        final WebsocketSenders websocketSenders = extender.getMetaDatum(WEBSOCKET_SENDERS);
        extender.appendProcessor(INIT, addWebsocketRegistryProcessor(websocketSenders, websocketRegistry));
        extender.appendProcessor(PRE_PROCESS, restoreWebsocketContextInformationProcessor());

        extender.routeIfEquals(PRE_PROCESS, jumpTo(AUTHORIZE_WEBSOCKET), REQUEST_TYPE, WEBSOCKET_AUTHORIZATION);
        extender.createChain(AUTHORIZE_WEBSOCKET, consume(), jumpTo(EXCEPTION_OCCURRED));
        final RuntimeConfigurationValue<WebsocketAuthorizer> websocketAuthorizer = runtimeConfiguration.create(websocketAuthorizerProvider);
        extender.appendProcessor(AUTHORIZE_WEBSOCKET, authorizeWebsocketProcessor(websocketAuthorizer));
        extender.appendProcessor(AUTHORIZE_WEBSOCKET, addAdditionalWebsocketDataProcessor(additionalWebsocketDataProvider));
        extender.appendProcessor(AUTHORIZE_WEBSOCKET, createWebsocketRegistryEntryProcessor(headerFilter, queryParameterFilter));

        extender.routeIfEquals(PRE_PROCESS, jumpTo(CONNECT_WEBSOCKET), REQUEST_TYPE, WEBSOCKET_CONNECT);
        extender.createChain(CONNECT_WEBSOCKET, drop(), jumpTo(EXCEPTION_OCCURRED));

        extender.appendProcessor(CONNECT_WEBSOCKET, putWebsocketInRegistryProcessor());

        extender.appendProcessor(PRE_DETERMINE_HANDLER, determineWebsocketRouteProcessor(routeSelectionExpression));

        extender.routeIfEquals(PRE_PROCESS, jumpTo(DISCONNECT_WEBSOCKET), REQUEST_TYPE, WEBSOCKET_DISCONNECT);
        extender.createChain(DISCONNECT_WEBSOCKET, drop(), jumpTo(EXCEPTION_OCCURRED));
        extender.appendProcessor(DISCONNECT_WEBSOCKET, removeWebsocketFromRegistryProcessor());
    }
}
