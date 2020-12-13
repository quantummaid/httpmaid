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

import de.quantummaid.httpmaid.http.Headers;
import de.quantummaid.httpmaid.http.HeadersBuilder;
import de.quantummaid.httpmaid.http.QueryParameters;
import de.quantummaid.httpmaid.http.QueryParametersBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class WebsocketEventUtils {

    private WebsocketEventUtils() {
    }

    public static QueryParameters extractQueryParameters(final AwsLambdaEvent event) {
        final Map<String, List<String>> queryParameters = event.getOrDefault("multiValueQueryStringParameters", HashMap::new);
        final QueryParametersBuilder builder = QueryParameters.builder();
        queryParameters.forEach(builder::withParameter);
        return builder.build();
    }

    public static Headers extractHeaders(final AwsLambdaEvent event) {
        final Map<String, List<String>> headers = event.getOrDefault("multiValueHeaders", HashMap::new);
        final HeadersBuilder headersBuilder = HeadersBuilder.headersBuilder();
        headersBuilder.withHeadersMap(headers);
        return headersBuilder.build();
    }
}
