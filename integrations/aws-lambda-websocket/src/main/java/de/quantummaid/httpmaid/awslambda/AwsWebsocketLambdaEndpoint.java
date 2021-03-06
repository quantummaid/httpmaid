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
import de.quantummaid.httpmaid.awslambda.sender.AwsWebsocketSender;
import de.quantummaid.httpmaid.awslambda.sender.apigateway.ApiGatewayClientFactory;
import de.quantummaid.httpmaid.chains.MetaDataKey;
import de.quantummaid.httpmaid.endpoint.RawResponse;
import de.quantummaid.httpmaid.websockets.authorization.AuthorizationDecision;
import de.quantummaid.httpmaid.websockets.endpoint.RawWebsocketConnectBuilder;
import de.quantummaid.httpmaid.websockets.registry.ConnectionInformation;
import de.quantummaid.httpmaid.websockets.registry.WebsocketRegistryEntry;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.LinkedHashMap;
import java.util.Map;

import static de.quantummaid.httpmaid.awslambda.AwsLambdaEvent.AWS_LAMBDA_EVENT;
import static de.quantummaid.httpmaid.awslambda.AwsLambdaEvent.awsLambdaEvent;
import static de.quantummaid.httpmaid.awslambda.AwsWebsocketAuthorizationException.awsWebsocketAuthorizationException;
import static de.quantummaid.httpmaid.awslambda.AwsWebsocketConnectionInformation.awsWebsocketConnectionInformation;
import static de.quantummaid.httpmaid.awslambda.MapDeserializer.mapFromString;
import static de.quantummaid.httpmaid.awslambda.authorizer.LambdaWebsocketAuthorizer.REGISTRY_ENTRY_KEY;
import static de.quantummaid.httpmaid.awslambda.authorizer.LambdaWebsocketAuthorizer.authorize;
import static de.quantummaid.httpmaid.awslambda.registry.EntryDeserializer.deserializeEntry;
import static de.quantummaid.httpmaid.awslambda.sender.AwsWebsocketSender.AWS_WEBSOCKET_SENDER;
import static de.quantummaid.httpmaid.awslambda.sender.AwsWebsocketSender.awsWebsocketSender;
import static de.quantummaid.httpmaid.awslambda.sender.apigateway.async.ApiGatewayAsyncClientFactory.defaultAsyncApiGatewayClientFactory;
import static de.quantummaid.httpmaid.closing.ClosingActions.CLOSING_ACTIONS;
import static de.quantummaid.httpmaid.util.Validators.validateNotNull;
import static de.quantummaid.httpmaid.util.Validators.validateNotNullNorEmpty;
import static de.quantummaid.httpmaid.websockets.WebsocketMetaDataKeys.WEBSOCKET_REGISTRY_ENTRY;
import static de.quantummaid.httpmaid.websockets.authorization.AuthorizationDecision.AUTHORIZATION_DECISION;
import static de.quantummaid.httpmaid.websockets.endpoint.RawWebsocketConnectBuilder.rawWebsocketConnectBuilder;
import static de.quantummaid.httpmaid.websockets.endpoint.RawWebsocketDisconnect.rawWebsocketDisconnect;
import static de.quantummaid.httpmaid.websockets.endpoint.RawWebsocketMessage.rawWebsocketMessage;
import static de.quantummaid.httpmaid.websockets.endpoint.RawWebsocketMessage.rawWebsocketMessageWithMetaData;
import static java.lang.String.format;
import static java.util.Collections.emptyMap;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class AwsWebsocketLambdaEndpoint {
    private static final String CONNECT_EVENT_TYPE = "CONNECT";
    private static final String DISCONNECT_EVENT_TYPE = "DISCONNECT";
    private static final String MESSAGE_EVENT_TYPE = "MESSAGE";
    private static final String REQUEST_CONTEXT_KEY = "requestContext";
    private static final String AUTHORIZER_KEY = "authorizer";

    private final HttpMaid httpMaid;
    private final String region;

    public static AwsWebsocketLambdaEndpoint awsWebsocketLambdaEndpointFor(final HttpMaid httpMaid,
                                                                           final String region) {
        return awsWebsocketLambdaEndpointFor(httpMaid, region, defaultAsyncApiGatewayClientFactory());
    }

    public static AwsWebsocketLambdaEndpoint awsWebsocketLambdaEndpointFor(
            final HttpMaid httpMaid,
            final String region,
            final ApiGatewayClientFactory apiGatewayClientFactory) {
        validateNotNull(httpMaid, "httpMaid");
        validateNotNullNorEmpty(region, "region");
        validateNotNull(apiGatewayClientFactory, "apiGatewayClientFactory");
        final AwsWebsocketSender websocketSender = awsWebsocketSender(apiGatewayClientFactory);
        httpMaid.addWebsocketSender(AWS_WEBSOCKET_SENDER, websocketSender);
        httpMaid.getMetaDatum(CLOSING_ACTIONS).addClosingAction(websocketSender);
        return new AwsWebsocketLambdaEndpoint(httpMaid, region);
    }

    public Map<String, Object> delegate(final Map<String, Object> event) {
        final AwsLambdaEvent awsLambdaEvent = awsLambdaEvent(event);
        return handleWebsocketRequest(awsLambdaEvent);
    }

    private Map<String, Object> handleWebsocketRequest(final AwsLambdaEvent event) {
        final AwsLambdaEvent requestContext = event.getMap(REQUEST_CONTEXT_KEY);
        final String eventType = requestContext.getAsString("eventType");
        final String connectionId = requestContext.getAsString("connectionId");
        final String stage = requestContext.getAsString("stage");
        final String apiId = requestContext.getAsString("apiId");
        final AwsWebsocketConnectionInformation connectionInformation = awsWebsocketConnectionInformation(connectionId, stage, apiId, region);
        if (CONNECT_EVENT_TYPE.equals(eventType)) {
            handleConnect(event, connectionInformation);
            return emptyMap();
        } else if (DISCONNECT_EVENT_TYPE.equals(eventType)) {
            handleDisconnect(connectionInformation, event);
            return emptyMap();
        } else if (MESSAGE_EVENT_TYPE.equals(eventType)) {
            return handleMessage(event, connectionInformation);
        } else {
            throw new UnsupportedOperationException(format("Unsupported lambda event type '%s' with event '%s'", eventType, event));
        }
    }

    private void handleConnect(final AwsLambdaEvent event,
                               final AwsWebsocketConnectionInformation connectionInformation) {
        final WebsocketRegistryEntry websocketRegistryEntry;
        if (isAlreadyAuthorized(event)) {
            websocketRegistryEntry = extractWebsocketRegistryEntry(event, connectionInformation);
        } else {
            final RawResponse authorizationResponse = authorize(event, httpMaid);
            final AuthorizationDecision authorizationDecision = authorizationResponse.metaData().get(AUTHORIZATION_DECISION);
            if (!authorizationDecision.isAuthorized()) {
                throw awsWebsocketAuthorizationException();
            }
            websocketRegistryEntry = authorizationResponse.metaData().get(WEBSOCKET_REGISTRY_ENTRY);
        }
        httpMaid.handleRequest(() -> {
            final RawWebsocketConnectBuilder builder = rawWebsocketConnectBuilder();
            builder.withConnectionInformation(AWS_WEBSOCKET_SENDER, connectionInformation);
            builder.withAdditionalMetaData(AWS_LAMBDA_EVENT, event);
            builder.withRegistryEntry(websocketRegistryEntry);
            return builder.build();
        }, ignored -> {
        });
    }

    private void handleDisconnect(final AwsWebsocketConnectionInformation connectionInformation,
                                  final AwsLambdaEvent event) {
        httpMaid.handleRequest(() -> rawWebsocketDisconnect(connectionInformation,
                Map.of(AWS_LAMBDA_EVENT, event)), response -> {
        });
    }

    private Map<String, Object> handleMessage(final AwsLambdaEvent event,
                                              final ConnectionInformation connectionInformation) {
        return httpMaid.handleRequestSynchronously(() -> {
            final String body = event.getAsString("body");
            final Map<MetaDataKey<?>, Object> additionalMetaData = Map.of(AWS_LAMBDA_EVENT, event);
            if (isAlreadyAuthorized(event)) {
                final WebsocketRegistryEntry registryEntry = extractWebsocketRegistryEntry(event, connectionInformation);
                return rawWebsocketMessageWithMetaData(connectionInformation, body, registryEntry, additionalMetaData);
            } else {
                return rawWebsocketMessage(connectionInformation, body, additionalMetaData);
            }
        }, response -> {
            final LinkedHashMap<String, Object> responseMap = new LinkedHashMap<>();
            response.optionalStringBody().ifPresent(s -> responseMap.put("body", s));
            return responseMap;
        });
    }

    private boolean isAlreadyAuthorized(final AwsLambdaEvent event) {
        final AwsLambdaEvent context = event.getMap(REQUEST_CONTEXT_KEY);
        return context.containsKey(AUTHORIZER_KEY);
    }

    private static WebsocketRegistryEntry extractWebsocketRegistryEntry(final AwsLambdaEvent event,
                                                                        final ConnectionInformation connectionInformation) {
        final AwsLambdaEvent authorizerContext = event.getMap(REQUEST_CONTEXT_KEY).getMap(AUTHORIZER_KEY);
        final String serializedRegistryEntry = authorizerContext
                .getAsString(REGISTRY_ENTRY_KEY);
        final Map<String, Object> map = mapFromString(serializedRegistryEntry);
        return deserializeEntry(connectionInformation, map);
    }
}
