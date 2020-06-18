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

package de.quantummaid.httpmaid.client.websocket.real;

import de.quantummaid.httpmaid.client.websocket.Websocket;
import de.quantummaid.httpmaid.client.websocket.WebsocketClient;
import de.quantummaid.httpmaid.client.websocket.WebsocketMessageHandler;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import static de.quantummaid.httpmaid.client.HttpMaidClientException.httpMaidClientException;
import static de.quantummaid.httpmaid.client.websocket.real.RealWebsocket.realWebsocket;
import static de.quantummaid.httpmaid.util.Validators.validateNotNull;
import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class RealWebsocketClient implements WebsocketClient {
    private final String uri;

    public static RealWebsocketClient realWebsocketClient(final String uri) {
        validateNotNull(uri, "uri");
        return new RealWebsocketClient(uri);
    }

    @Override
    public Websocket openWebsocket(final WebsocketMessageHandler messageHandler,
                                   final Map<String, List<String>> queryParameters,
                                   final Map<String, List<String>> headers,
                                   final String path) {
        final String fullUri = uri + path;
        final WebSocketClient client = new WebSocketClient();
        try {
            client.start();
            final URI uriObject = createUri(fullUri, queryParameters);
            final ClientUpgradeRequest request = new ClientUpgradeRequest();
            headers.forEach(request::setHeader);
            final RealWebsocket realWebsocket = realWebsocket(messageHandler, client);
            client.connect(realWebsocket, uriObject, request);
            realWebsocket.awaitConnect();
            return realWebsocket;
        } catch (final Exception e) {
            throw httpMaidClientException(e);
        }
    }

    private static URI createUri(final String rawUri,
                                 final Map<String, List<String>> queryParameters) throws URISyntaxException {
        final StringJoiner stringJoiner = new StringJoiner("&", rawUri + "?", "");
        queryParameters.forEach((name, values) ->
                values.forEach(value -> {
                    final String encodedName = URLEncoder.encode(name, UTF_8);
                    final String encodedValue = URLEncoder.encode(value, UTF_8);
                    stringJoiner.add(format("%s=%s", encodedName, encodedValue));
                }));
        final String fullUri = stringJoiner.toString();
        return new URI(fullUri);
    }
}
