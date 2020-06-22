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
import de.quantummaid.httpmaid.endpoint.RawHttpRequestBuilder;
import de.quantummaid.httpmaid.http.HeadersBuilder;
import de.quantummaid.httpmaid.http.QueryParameters;
import de.quantummaid.httpmaid.http.QueryParametersBuilder;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static de.quantummaid.httpmaid.awslambda.EventUtils.extractPotentiallyEncodedBody;
import static de.quantummaid.httpmaid.endpoint.RawHttpRequest.rawHttpRequestBuilder;

public final class RestApiHandler {

    private RestApiHandler() {
    }

    static Map<String, Object> handleRestApiRequest(final AwsLambdaEvent event,
                                                    final HttpMaid httpMaid) {
        return httpMaid.handleRequestSynchronously(() -> {
            final RawHttpRequestBuilder builder = rawHttpRequestBuilder();
            final String httpRequestMethod = event.getAsString("httpMethod");
            builder.withMethod(httpRequestMethod);

            final String encodedPath = event.getAsString("path");
            final String path = URLDecoder.decode(encodedPath, StandardCharsets.UTF_8);
            builder.withPath(path);

            builder.withPath(path);
            final Map<String, List<String>> headers = event.getOrDefault("multiValueHeaders", HashMap::new);
            final HeadersBuilder headersBuilder = HeadersBuilder.headersBuilder();
            headersBuilder.withHeadersMap(headers);
            builder.withHeaders(headersBuilder.build());

            final Map<String, List<String>> queryParameters = event.getOrDefault("multiValueQueryStringParameters", HashMap::new);
            final QueryParametersBuilder queryParametersBuilder = QueryParameters.builder();
            queryParameters.forEach(queryParametersBuilder::withParameter);
            builder.withQueryParameters(queryParametersBuilder.build());

            final String body = extractPotentiallyEncodedBody(event);
            builder.withBody(body);
            return builder.build();
        }, response -> {
            final int statusCode = response.status();
            final Map<String, List<String>> responseHeaders = response.headers();
            final String responseBody = response.stringBody();

            final LinkedHashMap<String, Object> responseMap = new LinkedHashMap<>();
            responseMap.put("statusCode", statusCode);
            responseMap.put("multiValueHeaders", responseHeaders);
            responseMap.put("body", responseBody);

            return responseMap;
        });
    }
}
