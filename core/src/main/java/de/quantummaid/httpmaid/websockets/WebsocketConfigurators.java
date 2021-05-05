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

import de.quantummaid.httpmaid.chains.Configurator;
import de.quantummaid.httpmaid.http.HeaderName;
import de.quantummaid.httpmaid.http.QueryParameterName;
import de.quantummaid.httpmaid.runtimeconfiguration.RuntimeConfigurationValueProvider;
import de.quantummaid.httpmaid.websockets.additionaldata.AdditionalWebsocketDataProvider;
import de.quantummaid.httpmaid.websockets.authorization.WebsocketAuthorizer;
import de.quantummaid.httpmaid.websockets.registry.WebsocketRegistry;
import de.quantummaid.httpmaid.websockets.registry.filter.header.HeaderFilter;
import de.quantummaid.httpmaid.websockets.registry.filter.queryparameter.QueryParameterFilter;

import java.util.List;

import static de.quantummaid.httpmaid.chains.Configurator.configuratorForType;
import static de.quantummaid.httpmaid.util.Validators.validateNotNull;
import static de.quantummaid.httpmaid.websockets.registry.filter.header.AllowAllHeaderFilter.allowAllHeaderFilter;
import static de.quantummaid.httpmaid.websockets.registry.filter.header.AllowListHeaderFilter.allowListHeaderFilter;
import static de.quantummaid.httpmaid.websockets.registry.filter.queryparameter.AllowListQueryParameterFilter.allowListQueryParameterFilter;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

public final class WebsocketConfigurators {

    private WebsocketConfigurators() {
    }

    public static Configurator toStoreAdditionalDataInWebsocketContext(final AdditionalWebsocketDataProvider dataProvider) {
        validateNotNull(dataProvider, "dataProvider");
        return configuratorForType(WebsocketsModule.class,
                websocketsModule -> websocketsModule.setAdditionalWebsocketDataProvider(dataProvider));
    }

    public static Configurator toSelectWebsocketRoutesBasedOn(final String routeSelectionExpression) {
        validateNotNull(routeSelectionExpression, "routeSelectionExpression");
        return configuratorForType(WebsocketsModule.class,
                websocketsModule -> websocketsModule.setRouteSelectionExpression(routeSelectionExpression));
    }

    public static Configurator toUseWebsocketRegistry(final WebsocketRegistry websocketRegistry) {
        return toUseWebsocketRegistry(() -> websocketRegistry);
    }

    public static Configurator toUseWebsocketRegistry(final RuntimeConfigurationValueProvider<WebsocketRegistry> websocketRegistry) {
        return configuratorForType(WebsocketsModule.class,
                websocketsModule -> websocketsModule.setWebsocketRegistryProvider(websocketRegistry));
    }

    public static Configurator toAuthorizeWebsocketsUsing(final WebsocketAuthorizer authorizer) {
        return toAuthorizeWebsocketsUsing(() -> authorizer);
    }

    public static Configurator toAuthorizeWebsocketsUsing(final RuntimeConfigurationValueProvider<WebsocketAuthorizer> authorizer) {
        validateNotNull(authorizer, "authorizer");
        return configuratorForType(WebsocketsModule.class,
                websocketsModule -> websocketsModule.setWebsocketAuthorizerProvider(authorizer));
    }

    public static Configurator toRememberAdditionalHeadersInWebsocketMessages(final String... headerNames) {
        final List<HeaderName> list = stream(headerNames)
                .map(HeaderName::headerName)
                .collect(toList());
        return toRememberAdditionalHeadersInWebsocketMessages(list);
    }

    public static Configurator toRememberAdditionalHeadersInWebsocketMessages(final List<HeaderName> headerNames) {
        return toFilterHeadersInWebsocketMessagesUsing(allowListHeaderFilter(headerNames));
    }

    public static Configurator toRememberAllHeadersInWebsocketMessages() {
        return toFilterHeadersInWebsocketMessagesUsing(allowAllHeaderFilter());
    }

    public static Configurator toFilterHeadersInWebsocketMessagesUsing(final HeaderFilter filter) {
        return configuratorForType(WebsocketsModule.class,
                websocketsModule -> websocketsModule.setHeaderFilter(filter));
    }

    public static Configurator toDropAllQueryParametersInWebsocketMessages() {
        return toRememberQueryParametersInWebsocketMessages();
    }

    public static Configurator toRememberQueryParametersInWebsocketMessages(final String... parameterNames) {
        final List<QueryParameterName> list = stream(parameterNames)
                .map(QueryParameterName::queryParameterName)
                .collect(toList());
        return toRememberQueryParametersInWebsocketMessages(list);
    }

    public static Configurator toRememberQueryParametersInWebsocketMessages(final List<QueryParameterName> parameterNames) {
        return toFilterQueryParametersInWebsocketMessagesUsing(allowListQueryParameterFilter(parameterNames));
    }

    public static Configurator toFilterQueryParametersInWebsocketMessagesUsing(final QueryParameterFilter filter) {
        return configuratorForType(WebsocketsModule.class,
                websocketsModule -> websocketsModule.setQueryParameterFilter(filter));
    }
}
