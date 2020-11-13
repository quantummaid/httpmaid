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
import de.quantummaid.httpmaid.websockets.registry.ConnectionInformation;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Map;

import static de.quantummaid.httpmaid.HttpMaidChainKeys.IS_HTTP_REQUEST;
import static de.quantummaid.httpmaid.HttpMaidChainKeys.REQUEST_BODY_STRING;
import static de.quantummaid.httpmaid.util.Validators.validateNotNull;
import static de.quantummaid.httpmaid.websockets.WebsocketMetaDataKeys.*;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class RawWebsocketMessage implements RawRequest {
    private final ConnectionInformation connectionInformation;
    private final String body;
    private final Map<MetaDataKey<?>, Object> additionalMetaData;

    public static RawWebsocketMessage rawWebsocketMessage(final ConnectionInformation connectionInformation,
                                                          final String body) {
        return rawWebsocketMessage(connectionInformation, body, Map.of());
    }

    public static RawWebsocketMessage rawWebsocketMessage(final ConnectionInformation connectionInformation,
                                                          final String body,
                                                          final Map<MetaDataKey<?>, Object> additionalMetaData) {
        validateNotNull(connectionInformation, "connectionInformation");
        validateNotNull(body, "body");
        validateNotNull(additionalMetaData, "additionalMetaData");
        return new RawWebsocketMessage(connectionInformation, body, additionalMetaData);
    }

    @Override
    public void enter(final MetaData metaData) {
        metaData.set(REQUEST_TYPE, WEBSOCKET_MESSAGE);
        metaData.set(IS_HTTP_REQUEST, false);
        metaData.set(WEBSOCKET_CONNECTION_INFORMATION, connectionInformation);
        metaData.set(REQUEST_BODY_STRING, body);
        additionalMetaData.forEach(metaData::setUnchecked);
    }
}
