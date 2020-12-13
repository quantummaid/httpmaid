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

package de.quantummaid.httpmaid.tests.deployers.fakeawslambda.websocket;

import org.eclipse.jetty.websocket.api.UpgradeRequest;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class EventUtils {

    private EventUtils() {
    }

    public static Map<String, Object> createWebsocketEvent(final String connectionId,
                                                           final String eventType,
                                                           final Object authorizerContext) {
        final Map<String, Object> requestContext = new HashMap<>();
        requestContext.put("eventType", eventType);
        requestContext.put("connectionId", connectionId);
        requestContext.put("stage", "fake");
        requestContext.put("apiId", "fake");
        requestContext.put("domainName", "fake.execute-api.fake-1.amazonaws.com");
        if (authorizerContext != null) {
            requestContext.put("authorizer", authorizerContext);
        }
        final Map<String, Object> event = new HashMap<>();
        event.put("requestContext", requestContext);
        return event;
    }

    public static void addFullWebsocketMetaData(final UpgradeRequest upgradeRequest,
                                                final Map<String, Object> event) {
        final Map<String, List<String>> queryParameters = new HashMap<>();
        final Map<String, List<String>> parameterMap = upgradeRequest.getParameterMap();
        parameterMap.forEach(queryParameters::put);
        event.put("multiValueQueryStringParameters", queryParameters);

        final Map<String, List<String>> headers = upgradeRequest.getHeaders();
        final Map<String, List<String>> normalizedHeaders = new HashMap<>();
        headers.forEach((name, values) -> {
            final List<String> normalizedValues;
            if (values.size() == 1) {
                final String[] splitValues = values.get(0).split(", ");
                normalizedValues = Arrays.asList(splitValues);
            } else {
                normalizedValues = values;
            }
            normalizedHeaders.put(name, normalizedValues);
        });
        event.put("multiValueHeaders", normalizedHeaders);
    }
}
