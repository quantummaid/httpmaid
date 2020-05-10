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
import de.quantummaid.httpmaid.endpoint.RawRequest;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.List;
import java.util.Map;

import static de.quantummaid.httpmaid.HttpMaidChainKeys.RAW_REQUEST_HEADERS;
import static de.quantummaid.httpmaid.HttpMaidChainKeys.RAW_REQUEST_QUERY_PARAMETERS;
import static de.quantummaid.httpmaid.websockets.WebsocketMetaDataKeys.*;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class RawWebsocketConnect implements RawRequest {
    private final Object connectionInformation;
    private final Map<String, String> queryParameters;
    private final Map<String, List<String>> headers;

    public static RawWebsocketConnect rawWebsocketConnect(final Object connectionInformation,
                                                          final Map<String, String> queryParameters,
                                                          final Map<String, List<String>> headers) {
        return new RawWebsocketConnect(connectionInformation, queryParameters, headers);
    }

    @Override
    public void enter(final MetaData metaData) {
        metaData.set(WEBSOCKET_CONNECTION_INFORMATION, connectionInformation);
        metaData.set(REQUEST_TYPE, WEBSOCKET_CONNECT);
        metaData.set(RAW_REQUEST_QUERY_PARAMETERS, queryParameters);
        metaData.set(RAW_REQUEST_HEADERS, headers);
    }
}
