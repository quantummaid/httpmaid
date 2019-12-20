/*
 * Copyright (c) 2019 Richard Hauswald - https://quantummaid.de/.
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

package de.quantummaid.httpmaid.websockets.processors;

import de.quantummaid.httpmaid.websockets.WebSocketMapping;
import de.quantummaid.httpmaid.chains.MetaData;
import de.quantummaid.httpmaid.chains.Processor;
import de.quantummaid.httpmaid.path.Path;
import de.quantummaid.httpmaid.path.PathTemplate;
import de.quantummaid.httpmaid.http.PathParameters;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.List;
import java.util.Map;

import static de.quantummaid.httpmaid.HttpMaidChainKeys.PATH;
import static de.quantummaid.httpmaid.HttpMaidChainKeys.PATH_PARAMETERS;
import static de.quantummaid.httpmaid.http.PathParameters.pathParameters;
import static de.quantummaid.httpmaid.util.Validators.validateNotNull;
import static de.quantummaid.httpmaid.websockets.WebsocketChainKeys.WEBSOCKET_MAPPING;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class DetermineWebSocketTypeProcessor implements Processor {
    private final List<WebSocketMapping> webSocketMappings;

    public static Processor determineWebSocketTypeProcessor(final List<WebSocketMapping> webSocketMappings) {
        validateNotNull(webSocketMappings, "webSocketMappings");
        return new DetermineWebSocketTypeProcessor(webSocketMappings);
    }

    @Override
    public void apply(final MetaData metaData) {
        final Path path = metaData.get(PATH);
        webSocketMappings.stream()
                .filter(webSocketMapping -> webSocketMapping.pathTemplate().matches(path))
                .findFirst()
                .ifPresent(webSocketMapping -> {
                    metaData.set(WEBSOCKET_MAPPING, webSocketMapping);
                    final PathTemplate pathTemplate = webSocketMapping.pathTemplate();
                    final Map<String, String> pathParametersMap = pathTemplate.extractPathParameters(path);
                    final PathParameters pathParameters = pathParameters(pathParametersMap);
                    metaData.set(PATH_PARAMETERS, pathParameters);
                });
    }
}
