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

package de.quantummaid.httpmaid.awslambda;

import de.quantummaid.httpmaid.HttpMaid;
import de.quantummaid.httpmaid.http.HeadersBuilder;
import de.quantummaid.httpmaid.websockets.endpoint.RawWebsocketConnectBuilder;
import de.quantummaid.httpmaid.websockets.endpoint.RawWebsocketMessage;
import de.quantummaid.httpmaid.websockets.registry.ConnectionInformation;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static de.quantummaid.httpmaid.awslambda.AwsLambdaEvent.awsLambdaEvent;
import static de.quantummaid.httpmaid.awslambda.AwsLambdaEventKeys.MULTIVALUE_HEADERS;
import static de.quantummaid.httpmaid.awslambda.AwsLambdaEventKeys.QUERY_STRING_PARAMETERS;
import static de.quantummaid.httpmaid.awslambda.AwsWebsocketConnectionInformation.awsWebsocketConnectionInformation;
import static de.quantummaid.httpmaid.awslambda.AwsWebsocketSender.AWS_WEBSOCKET_SENDER;
import static de.quantummaid.httpmaid.util.Validators.validateNotNull;
import static de.quantummaid.httpmaid.websockets.endpoint.RawWebsocketConnectBuilder.rawWebsocketConnectBuilder;
import static java.util.Collections.emptyMap;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class AwsWebsocketLambdaEndpoint {
    private static final String CONNECT_EVENT_TYPE = "CONNECT";
    private static final String DISCONNECT_EVENT_TYPE = "DISCONNECT";
    private static final String MESSAGE_EVENT_TYPE = "MESSAGE";

    private final HttpMaid httpMaid;

    public static AwsWebsocketLambdaEndpoint awsWebsocketLambdaEndpointFor(final HttpMaid httpMaid) {
        validateNotNull(httpMaid, "httpMaid");
        return new AwsWebsocketLambdaEndpoint(httpMaid);
    }

    public Map<String, Object> delegate(final Map<String, Object> event) {
        final AwsLambdaEvent awsLambdaEvent = awsLambdaEvent(event);
        return handleWebsocketRequest(awsLambdaEvent);
    }

    private Map<String, Object> handleWebsocketRequest(final AwsLambdaEvent event) {
        final String eventType = event.getFromContext("eventType");
        final String connectionId = event.getFromContext("connectionId");
        final String stage = event.getFromContext("stage");
        final String apiId = event.getFromContext("apiId");
        final String domainName = event.getFromContext("domainName");
        final String region = extractRegionFromDomain(domainName);
        final AwsWebsocketConnectionInformation connectionInformation = awsWebsocketConnectionInformation(connectionId, stage, apiId, region);
        if (CONNECT_EVENT_TYPE.equals(eventType)) {
            handleConnect(event, connectionInformation);
            return emptyMap();
        } else if (DISCONNECT_EVENT_TYPE.equals(eventType)) {
            httpMaid.handleWebsocketDisconnect();
            return emptyMap();
        } else if (MESSAGE_EVENT_TYPE.equals(eventType)) {
            return handleMessage(event, connectionInformation);
        } else {
            throw new UnsupportedOperationException(String.format("Unsupported lambda event type '%s' with event '%s'", eventType, event));
        }
    }

    private void handleConnect(final AwsLambdaEvent event,
                               final AwsWebsocketConnectionInformation connectionInformation) {
        httpMaid.handleRequest(() -> {
            final RawWebsocketConnectBuilder builder = rawWebsocketConnectBuilder();
            builder.withConnectionInformation(AWS_WEBSOCKET_SENDER, connectionInformation);

            final Map<String, List<String>> queryParameters = event.getOrDefault(QUERY_STRING_PARAMETERS, HashMap::new);
            builder.withQueryParameterMap(queryParameters);

            final Map<String, List<String>> headers = event.getOrDefault(MULTIVALUE_HEADERS, HashMap::new);
            final HeadersBuilder headersBuilder = HeadersBuilder.headersBuilder();
            headersBuilder.withHeadersMap(headers);
            builder.withHeaders(headersBuilder.build());

            return builder.build();
        }, response -> {
        });
    }

    private Map<String, Object> handleMessage(final AwsLambdaEvent event,
                                                       final ConnectionInformation connectionInformation) {
        return httpMaid.handleRequestSynchronously(() -> {
            final String body = event.getAsString("body");
            return RawWebsocketMessage.rawWebsocketMessage(connectionInformation, body);
        }, response -> {
            final LinkedHashMap<String, Object> responseMap = new LinkedHashMap<>();
            response.optionalStringBody().ifPresent(s -> responseMap.put("body", s));
            return responseMap;
        });
    }

    private static String extractRegionFromDomain(final String domain) {
        return domain.split("\\.")[2];
    }
}
