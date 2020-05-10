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

package de.quantummaid.httpmaid.websockets.processors;

import de.quantummaid.httpmaid.chains.MetaData;
import de.quantummaid.httpmaid.chains.Processor;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Map;

import static de.quantummaid.httpmaid.HttpMaidChainKeys.UNMARSHALLED_REQUEST_BODY;
import static de.quantummaid.httpmaid.util.Validators.validateNotNull;
import static de.quantummaid.httpmaid.websockets.WebsocketMetaDataKeys.WEBSOCKET_ROUTE;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class DetermineWebsocketRouteProcessor implements Processor {
    private final String routeSelectionExpression;

    public static Processor determineWebsocketRouteProcessor(final String routeSelectionExpression) {
        validateNotNull(routeSelectionExpression, "routeSelectionExpression");
        return new DetermineWebsocketRouteProcessor(routeSelectionExpression);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void apply(final MetaData metaData) {
        metaData.getOptional(UNMARSHALLED_REQUEST_BODY).ifPresent(unmarshalledRequestBody -> {
            if (!(unmarshalledRequestBody instanceof Map)) {
                return;
            }
            final Map<String, Object> map = (Map<String, Object>) unmarshalledRequestBody;
            if (!map.containsKey(routeSelectionExpression)) {
                return;
            }
            final Object category = map.get(routeSelectionExpression);
            if (category instanceof String) {
                metaData.set(WEBSOCKET_ROUTE, (String) category);
            }
        });
    }
}
