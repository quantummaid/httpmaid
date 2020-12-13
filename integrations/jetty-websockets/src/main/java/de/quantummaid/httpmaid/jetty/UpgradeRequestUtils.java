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

package de.quantummaid.httpmaid.jetty;

import de.quantummaid.httpmaid.http.Headers;
import de.quantummaid.httpmaid.http.HeadersBuilder;
import org.eclipse.jetty.websocket.api.UpgradeRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.quantummaid.httpmaid.http.HeadersBuilder.headersBuilder;

public final class UpgradeRequestUtils {

    private UpgradeRequestUtils() {
    }

    public static Headers extractHeaders(final UpgradeRequest upgradeRequest) {
        final HeadersBuilder headersBuilder = headersBuilder();
        final Map<String, List<String>> headersMap = upgradeRequest.getHeaders();
        headersBuilder.withHeadersMap(headersMap);
        return headersBuilder.build();
    }

    public static Map<String, List<String>> extractQueryParameters(final UpgradeRequest upgradeRequest) {
        final Map<String, List<String>> queryParameters = new HashMap<>();
        final Map<String, List<String>> parameterMap = upgradeRequest.getParameterMap();
        parameterMap.forEach(queryParameters::put);
        return queryParameters;
    }
}
