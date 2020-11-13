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
import de.quantummaid.httpmaid.endpoint.RawResponse;
import de.quantummaid.httpmaid.http.HeadersBuilder;
import de.quantummaid.httpmaid.http.QueryParameters;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static de.quantummaid.httpmaid.awslambda.EventUtils.extractPotentiallyEncodedBody;
import static de.quantummaid.httpmaid.awslambda.RequestBuilderFactory.createRequestBuilder;
import static de.quantummaid.httpmaid.endpoint.RawHttpRequest.rawHttpRequestBuilder;
import static de.quantummaid.httpmaid.http.HeadersBuilder.headersBuilder;
import static de.quantummaid.httpmaid.http.Http.Headers.COOKIE;
import static de.quantummaid.httpmaid.http.Http.Headers.SET_COOKIE;
import static java.util.Arrays.stream;

public final class HttpApiHandler {

    private HttpApiHandler() {
    }

    static Map<String, Object> handleHttpApiRequest(final AwsLambdaEvent event,
                                                    final HttpMaid httpMaid) {
        return httpMaid.handleRequestSynchronously(() -> {
            final RawHttpRequestBuilder builder = createRequestBuilder(event);

            final AwsLambdaEvent requestContext = event.getMap("requestContext");
            final AwsLambdaEvent httpInformation = requestContext.getMap("http");
            final String httpRequestMethod = httpInformation.getAsString("method");
            builder.withMethod(httpRequestMethod);
            final String path = httpInformation.getAsString("path");
            builder.withPath(path);

            final Map<String, String> headers = event.getOrDefault("headers", LinkedHashMap::new);
            final HeadersBuilder headersBuilder = headersBuilder();
            headers.forEach((key, commaSeparatedValues) -> {
                final String[] values = commaSeparatedValues.split(",");
                stream(values).forEach(value -> headersBuilder.withAdditionalHeader(key, value));
            });
            final List<String> cookies = event.getOrDefault("cookies", ArrayList::new);
            cookies.forEach(cookie -> headersBuilder.withAdditionalHeader(COOKIE, cookie));
            builder.withHeaders(headersBuilder.build());

            final String queryString = event.getAsString("rawQueryString");
            final QueryParameters queryParameters = QueryParameters.fromQueryString(queryString);
            builder.withQueryParameters(queryParameters);

            final String body = extractPotentiallyEncodedBody(event);
            builder.withBody(body);

            return builder.build();
        }, response -> {
            final LinkedHashMap<String, Object> responseMap = new LinkedHashMap<>();
            final int statusCode = response.status();
            responseMap.put("statusCode", statusCode);
            final String responseBody = response.stringBody();
            responseMap.put("body", responseBody);
            setHeadersInResponse(response, responseMap);
            return responseMap;
        });
    }

    private static void setHeadersInResponse(final RawResponse response,
                                             final Map<String, Object> responseMap) {
        final List<String> cookies = new ArrayList<>();
        final Map<String, String> singleHeaders = new LinkedHashMap<>();
        final Map<String, List<String>> responseHeaders = response.headers();
        responseHeaders.forEach((key, values) -> {
            if (key.equalsIgnoreCase(SET_COOKIE)) {
                cookies.addAll(values);
            } else {
                final String joinedValues = String.join(",", values);
                singleHeaders.put(key, joinedValues);
            }
        });

        responseMap.put("headers", singleHeaders);
        responseMap.put("cookies", cookies);
    }
}
