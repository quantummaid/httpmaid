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
import de.quantummaid.httpmaid.handler.http.HttpRequest;
import de.quantummaid.httpmaid.websockets.additionaldata.AdditionalWebsocketDataProvider;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;

import static de.quantummaid.httpmaid.websockets.WebsocketMetaDataKeys.ADDITIONAL_WEBSOCKET_DATA;
import static de.quantummaid.httpmaid.websockets.authorization.AuthorizationDecision.AUTHORIZATION_DECISION;
import static de.quantummaid.reflectmaid.validators.NotNullValidator.validateNotNull;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class AddAdditionalWebsocketDataProcessor implements Processor {
    private final AdditionalWebsocketDataProvider dataProvider;

    public static AddAdditionalWebsocketDataProcessor addAdditionalWebsocketDataProcessor(
            final AdditionalWebsocketDataProvider dataProvider
    ) {
        validateNotNull(dataProvider, "dataProvider");
        return new AddAdditionalWebsocketDataProcessor(dataProvider);
    }

    @Override
    public void apply(final MetaData metaData) {
        metaData.getOptional(AUTHORIZATION_DECISION).ifPresent(authorizationDecision -> {
            if (!authorizationDecision.isAuthorized()) {
                return;
            }
            final Map<String, Object> additionalData = metaData.getOptional(ADDITIONAL_WEBSOCKET_DATA)
                    .map(LinkedHashMap::new)
                    .orElseGet(LinkedHashMap::new);

            final HttpRequest httpRequest = HttpRequest.httpRequest(metaData);
            final Map<String, Object> providedData = dataProvider.provide(httpRequest);
            additionalData.putAll(providedData);

            metaData.set(ADDITIONAL_WEBSOCKET_DATA, additionalData);
        });
    }
}
