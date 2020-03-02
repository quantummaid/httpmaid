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

import de.quantummaid.httpmaid.websockets.builder.CategorizerStage;
import de.quantummaid.httpmaid.chains.*;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import static de.quantummaid.httpmaid.path.PathTemplate.pathTemplate;
import static java.util.Collections.singletonList;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class WebSocketsConfigurator implements Configurator {
    private final List<WebSocketMapping> webSocketMappings = new LinkedList<>();

    public static WebSocketsConfigurator toUseWebSockets() {
        return new WebSocketsConfigurator();
    }

    public CategorizerStage acceptingWebSocketsToThePath(final String template) {
        return new CategorizerStage() {
            @Override
            public <T> WebSocketsConfigurator initializingMetaDataForIncomingMessagesWith(
                    final MetaDataKey<T> key,
                    final Function<MetaData, T> valueProvider) {
                final MetaDataEntryProvider<T> provider = MetaDataEntryProvider.storing(key, valueProvider);
                final MetaDataEntriesToSave metaDataEntriesToSave = MetaDataEntriesToSave.metaDataEntriesToSave(singletonList(provider));
                final WebSocketMapping webSocketMapping = WebSocketMapping.webSocketMapping(metaDataEntriesToSave, pathTemplate(template));
                webSocketMappings.add(webSocketMapping);
                return WebSocketsConfigurator.this;
            }
        };
    }

    @Override
    public List<ChainModule> supplyModulesIfNotAlreadyPreset() {
        return Arrays.asList(WebSocketModule.webSocketModule());
    }

    @Override
    public void configure(final DependencyRegistry dependencyRegistry) {
        final WebSocketModule webSocketModule = dependencyRegistry.getDependency(WebSocketModule.class);
        webSocketMappings.forEach(webSocketModule::addWebSocketMapping);
    }
}
