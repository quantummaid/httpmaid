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

import de.quantummaid.httpmaid.websockets.registry.ConnectionInformation;
import de.quantummaid.httpmaid.websockets.sender.NonSerializableConnectionInformation;
import de.quantummaid.httpmaid.websockets.sender.WebsocketSenderId;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.quantummaid.httpmaid.websockets.endpoint.RawWebsocketConnect.rawWebsocketConnect;
import static de.quantummaid.httpmaid.websockets.sender.NonSerializableWebsocketSender.NON_SERIALIZABLE_WEBSOCKET_SENDER;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class RawWebsocketConnectBuilder {
    private ConnectionInformation connectionInformation;
    private WebsocketSenderId websocketSenderId;
    private Map<String, List<String>> headers = new HashMap<>();
    private Map<String, String> queryParameters = new HashMap<>();

    public static RawWebsocketConnectBuilder rawWebsocketConnectBuilder() {
        return new RawWebsocketConnectBuilder();
    }

    public RawWebsocketConnectBuilder withNonSerializableConnectionInformation(final NonSerializableConnectionInformation connectionInformation) {
        return withConnectionInformation(NON_SERIALIZABLE_WEBSOCKET_SENDER, connectionInformation);
    }

    public RawWebsocketConnectBuilder withConnectionInformation(final WebsocketSenderId websocketSenderId,
                                                                final ConnectionInformation connectionInformation) {
        this.websocketSenderId = websocketSenderId;
        this.connectionInformation = connectionInformation;
        return this;
    }

    public RawWebsocketConnectBuilder withHeaders(final Map<String, List<String>> headers) {
        this.headers = headers;
        return this;
    }

    public RawWebsocketConnectBuilder withEncodedQueryParameters(final String encodedQueryParameters) {
        final Map<String, String> queryParametersMap = queryToMap(encodedQueryParameters);
        return withUniqueQueryParameters(queryParametersMap);
    }

    public RawWebsocketConnectBuilder withUniqueQueryParameters(final Map<String, String> queryParameters) {
        this.queryParameters = queryParameters;
        return this;
    }

    public RawWebsocketConnect build() {
        return rawWebsocketConnect(connectionInformation, websocketSenderId, queryParameters, headers);
    }

    private static Map<String, String> queryToMap(final String query) {
        final Map<String, String> result = new HashMap<>();
        if (query == null || query.isEmpty()) {
            return result;
        }
        for (final String param : query.split("&")) {
            final String[] entry = param.split("=");
            if (entry.length > 1) {
                result.put(entry[0], entry[1]);
            } else {
                result.put(entry[0], "");
            }
        }
        return result;
    }
}
