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
import de.quantummaid.httpmaid.http.Header;
import de.quantummaid.httpmaid.http.Headers;
import de.quantummaid.httpmaid.http.QueryParameters;
import de.quantummaid.httpmaid.websockets.registry.ConnectionInformation;
import de.quantummaid.httpmaid.websockets.registry.HeaderFilter;
import de.quantummaid.httpmaid.websockets.registry.WebsocketRegistryEntry;
import de.quantummaid.httpmaid.websockets.sender.WebsocketSenderId;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

import static de.quantummaid.httpmaid.HttpMaidChainKeys.QUERY_PARAMETERS;
import static de.quantummaid.httpmaid.HttpMaidChainKeys.REQUEST_HEADERS;
import static de.quantummaid.httpmaid.http.Headers.headers;
import static de.quantummaid.httpmaid.websockets.WebsocketMetaDataKeys.ADDITIONAL_WEBSOCKET_DATA;
import static de.quantummaid.httpmaid.websockets.WebsocketMetaDataKeys.WEBSOCKET_REGISTRY_ENTRY;
import static de.quantummaid.httpmaid.websockets.registry.ConnectionInformation.dummyConnectionInformation;
import static de.quantummaid.httpmaid.websockets.registry.WebsocketRegistryEntry.websocketRegistryEntry;
import static de.quantummaid.httpmaid.websockets.sender.WebsocketSenderId.WEBSOCKET_SENDER_ID;
import static de.quantummaid.reflectmaid.validators.NotNullValidator.validateNotNull;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class CreateWebsocketRegistryEntryProcessor implements Processor {
    private final HeaderFilter headerFilter;

    public static CreateWebsocketRegistryEntryProcessor createWebsocketRegistryEntryProcessor(final HeaderFilter headerFilter) {
        validateNotNull(headerFilter, "headerFilter");
        return new CreateWebsocketRegistryEntryProcessor(headerFilter);
    }

    @Override
    public void apply(final MetaData metaData) {
        final ConnectionInformation connectionInformation = dummyConnectionInformation();
        final WebsocketSenderId senderId = metaData.get(WEBSOCKET_SENDER_ID);
        final Headers headers = metaData.get(REQUEST_HEADERS);
        final List<Header> filteredHeaders = headerFilter.filter(headers.asList());
        final QueryParameters queryParameters = metaData.get(QUERY_PARAMETERS);
        final Map<String, Object> additionalData = metaData.get(ADDITIONAL_WEBSOCKET_DATA);
        final WebsocketRegistryEntry entry = websocketRegistryEntry(
                connectionInformation,
                senderId,
                headers(filteredHeaders),
                queryParameters,
                additionalData
        );
        metaData.set(WEBSOCKET_REGISTRY_ENTRY, entry);
    }
}
