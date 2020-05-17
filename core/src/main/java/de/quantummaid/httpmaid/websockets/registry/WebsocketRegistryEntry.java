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

import static de.quantummaid.httpmaid.HttpMaidChainKeys.*;
import static de.quantummaid.httpmaid.util.Validators.validateNotNull;
import static de.quantummaid.httpmaid.websockets.WebsocketMetaDataKeys.WEBSOCKET_CONNECTION_INFORMATION;
import static de.quantummaid.httpmaid.websockets.sender.WebsocketSenderId.WEBSOCKET_SENDER_ID;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class WebsocketRegistryEntry {
    private final Object connectionInformation;
    private final WebsocketSenderId senderId;
    private final Headers headers;
    private final ContentType contentType;
    private final QueryParameters queryParameters;

    private static WebsocketRegistryEntry websocketRegistryEntry(final Object connectionInformation,
                                                                 final WebsocketSenderId senderId,
                                                                 final Headers headers,
                                                                 final ContentType contentType,
                                                                 final QueryParameters queryParameters) {
        validateNotNull(connectionInformation, "connectionInformation");
        validateNotNull(senderId, "senderId");
        validateNotNull(headers, "headers");
        validateNotNull(contentType, "contentType");
        validateNotNull(queryParameters, "queryParameters");
        return new WebsocketRegistryEntry(connectionInformation, senderId, headers, contentType, queryParameters);
    }

    public static WebsocketRegistryEntry loadFromMetaData(final MetaData metaData) {
        final Object connectionInformation = metaData.get(WEBSOCKET_CONNECTION_INFORMATION);
        final WebsocketSenderId senderId = metaData.get(WEBSOCKET_SENDER_ID);
        final Headers headers = metaData.get(REQUEST_HEADERS);
        final ContentType contentType = metaData.get(REQUEST_CONTENT_TYPE);
        final QueryParameters queryParameters = metaData.get(QUERY_PARAMETERS);
        return websocketRegistryEntry(connectionInformation, senderId, headers, contentType, queryParameters);
    }

    public WebsocketSenderId senderId() {
        return senderId;
    }

    public Object connectionInformation() {
        return connectionInformation;
    }

    public Headers headers() {
        return headers;
    }

    public ContentType contentType() {
        return contentType;
    }

    public void restoreToMetaData(final MetaData metaData) {
        metaData.set(REQUEST_HEADERS, headers);
        metaData.set(REQUEST_CONTENT_TYPE, contentType);
        metaData.set(QUERY_PARAMETERS, queryParameters);
    }
}
