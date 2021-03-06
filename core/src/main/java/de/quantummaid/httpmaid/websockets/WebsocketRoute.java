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

import de.quantummaid.httpmaid.chains.MetaData;
import de.quantummaid.httpmaid.generator.GenerationCondition;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import static de.quantummaid.httpmaid.util.Validators.validateNotNull;
import static de.quantummaid.httpmaid.websockets.WebsocketMetaDataKeys.*;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class WebsocketRoute implements GenerationCondition {
    private final String route;

    public static GenerationCondition webSocketCategory(final String route) {
        validateNotNull(route, "route");
        return new WebsocketRoute(route);
    }

    @Override
    public boolean generate(final MetaData metaData) {
        final boolean websocket = metaData.getOptional(REQUEST_TYPE)
                .map(WEBSOCKET_MESSAGE::equals)
                .orElse(false);
        if (!websocket) {
            return false;
        }
        return metaData.getOptional(WEBSOCKET_ROUTE)
                .map(route::equals)
                .orElse(false);
    }
}
