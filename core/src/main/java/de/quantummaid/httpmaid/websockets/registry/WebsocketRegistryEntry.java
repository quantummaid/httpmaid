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

package de.quantummaid.httpmaid.websockets.registry;

import de.quantummaid.httpmaid.chains.MetaData;
import de.quantummaid.httpmaid.http.Headers;
import de.quantummaid.httpmaid.http.QueryParameters;
import de.quantummaid.httpmaid.http.headers.ContentType;
import de.quantummaid.httpmaid.websockets.sender.WebsocketSenderId;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Map;
import java.util.Optional;

import static de.quantummaid.httpmaid.HttpMaidChainKeys.*;
import static de.quantummaid.httpmaid.util.Validators.validateNotNull;
import static de.quantummaid.httpmaid.websockets.WebsocketMetaDataKeys.ADDITIONAL_WEBSOCKET_DATA;
import static de.quantummaid.httpmaid.websockets.WebsocketMetaDataKeys.WEBSOCKET_CONNECTION_INFORMATION;
import static de.quantummaid.httpmaid.websockets.sender.WebsocketSenderId.WEBSOCKET_SENDER_ID;
import static de.quantummaid.httpmaid.websockets.sender.WebsocketSenderId.websocketSenderId;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class WebsocketRegistryEntry {
    private final ConnectionInformation connectionInformation;
    private final WebsocketSenderId senderId;
    private final Headers headers;
    private final ContentType contentType;
    private final QueryParameters queryParameters;
    private final Map<String, Object> additionalData;

    public static WebsocketRegistryEntry websocketRegistryEntry(final ConnectionInformation connectionInformation,
                                                                final WebsocketSenderId senderId,
                                                                final Headers headers,
                                                                final ContentType contentType,
                                                                final QueryParameters queryParameters,
                                                                final Map<String, Object> additionalData) {
        validateNotNull(connectionInformation, "connectionInformation");
        validateNotNull(senderId, "senderId");
        validateNotNull(headers, "headers");
        validateNotNull(contentType, "contentType");
        validateNotNull(queryParameters, "queryParameters");
        validateNotNull(additionalData, "additionalData");
        return new WebsocketRegistryEntry(
                connectionInformation,
                senderId,
                headers,
                contentType,
                queryParameters,
                additionalData
        );
    }

    public static WebsocketRegistryEntry loadFromMetaData(final MetaData metaData) {
        final ConnectionInformation connectionInformation = metaData.get(WEBSOCKET_CONNECTION_INFORMATION);
        final WebsocketSenderId senderId = metaData.get(WEBSOCKET_SENDER_ID);
        final Headers headers = metaData.get(REQUEST_HEADERS);
        final ContentType contentType = metaData.get(REQUEST_CONTENT_TYPE);
        final QueryParameters queryParameters = metaData.get(QUERY_PARAMETERS);
        final Map<String, Object> additionalData = metaData.get(ADDITIONAL_WEBSOCKET_DATA);
        return websocketRegistryEntry(connectionInformation, senderId, headers, contentType, queryParameters, additionalData);
    }

    public static WebsocketRegistryEntry restoreFromStrings(final ConnectionInformation connectionInformation,
                                                            final String senderId,
                                                            final Headers headers,
                                                            final Optional<String> contentType,
                                                            final QueryParameters queryParameters,
                                                            final Map<String, Object> additionalData) {
        final WebsocketSenderId websocketSenderId = websocketSenderId(senderId);
        final ContentType contentTypeObject = ContentType.fromString(contentType);
        return websocketRegistryEntry(
                connectionInformation,
                websocketSenderId,
                headers,
                contentTypeObject,
                queryParameters,
                additionalData
        );
    }

    public WebsocketSenderId senderId() {
        return senderId;
    }

    public ConnectionInformation connectionInformation() {
        return connectionInformation;
    }

    public WebsocketSenderId getSenderId() {
        return senderId;
    }

    public Headers headers() {
        return headers;
    }

    public ContentType contentType() {
        return contentType;
    }

    public QueryParameters queryParameters() {
        return queryParameters;
    }

    public Map<String, Object> additionalData() {
        return additionalData;
    }

    public void restoreToMetaData(final MetaData metaData) {
        metaData.set(REQUEST_HEADERS, headers);
        metaData.set(REQUEST_CONTENT_TYPE, contentType);
        metaData.set(QUERY_PARAMETERS, queryParameters);
        metaData.set(ADDITIONAL_WEBSOCKET_DATA, additionalData);
    }
}
