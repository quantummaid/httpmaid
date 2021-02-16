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

package de.quantummaid.httpmaid.websockets.endpoint;

import de.quantummaid.httpmaid.chains.MetaData;
import de.quantummaid.httpmaid.chains.MetaDataKey;
import de.quantummaid.httpmaid.endpoint.RawRequest;
import de.quantummaid.httpmaid.http.Headers;
import de.quantummaid.httpmaid.http.QueryParameters;
import de.quantummaid.httpmaid.websockets.sender.WebsocketSenderId;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.Map;

import static de.quantummaid.httpmaid.HttpMaidChainKeys.QUERY_PARAMETERS;
import static de.quantummaid.httpmaid.HttpMaidChainKeys.REQUEST_HEADERS;
import static de.quantummaid.httpmaid.websockets.WebsocketMetaDataKeys.REQUEST_TYPE;
import static de.quantummaid.httpmaid.websockets.WebsocketMetaDataKeys.WEBSOCKET_AUTHORIZATION;
import static de.quantummaid.httpmaid.websockets.sender.WebsocketSenderId.WEBSOCKET_SENDER_ID;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class RawWebsocketAuthorization implements RawRequest {
    private final QueryParameters queryParameters;
    private final Headers headers;
    private final WebsocketSenderId websocketSenderId;
    private final Map<MetaDataKey<?>, Object> additionalMetaData;

    public static RawWebsocketAuthorization rawWebsocketAuthorization(final QueryParameters queryParameters,
                                                                      final Headers headers,
                                                                      final WebsocketSenderId websocketSenderId,
                                                                      final Map<MetaDataKey<?>, Object> additionalMetaData) {
        return new RawWebsocketAuthorization(queryParameters, headers, websocketSenderId, additionalMetaData);
    }

    @Override
    public void enter(final MetaData metaData) {
        metaData.set(REQUEST_TYPE, WEBSOCKET_AUTHORIZATION);
        metaData.set(QUERY_PARAMETERS, queryParameters);
        metaData.set(REQUEST_HEADERS, headers);
        metaData.set(WEBSOCKET_SENDER_ID, websocketSenderId);
        additionalMetaData.forEach(metaData::setUnchecked);
    }
}
