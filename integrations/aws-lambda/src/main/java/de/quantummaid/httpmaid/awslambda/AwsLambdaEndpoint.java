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
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static de.quantummaid.httpmaid.awslambda.AwsLambdaEvent.awsLambdaEvent;
import static de.quantummaid.httpmaid.awslambda.AwsLambdaEventKeys.*;
import static de.quantummaid.httpmaid.endpoint.RawHttpRequest.rawHttpRequestBuilder;
import static de.quantummaid.httpmaid.http.HeadersBuilder.headersBuilder;
import static de.quantummaid.httpmaid.util.Validators.validateNotNull;
import static java.util.stream.Collectors.joining;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class AwsLambdaEndpoint {
    private final HttpMaid httpMaid;

    public static AwsLambdaEndpoint awsLambdaEndpointFor(final HttpMaid httpMaid) {
        validateNotNull(httpMaid, "httpMaid");
        return new AwsLambdaEndpoint(httpMaid);
    }

    public Map<String, Object> delegate(final Map<String, Object> event) {
        final AwsLambdaEvent awsLambdaEvent = awsLambdaEvent(event);

        if (event.containsKey("version")) {
            return handleHttpApiRequest(awsLambdaEvent);
        } else {
            return handleRestApiRequest(awsLambdaEvent);
        }
    }

    private Map<String, Object> handleHttpApiRequest(final AwsLambdaEvent event) {
        return httpMaid.handleRequestSynchronously(() -> {
            final RawHttpRequestBuilder builder = rawHttpRequestBuilder();
            Map<String, Object> requestContext = event.getMap("requestContext");
            Map<String, Object> httpInformation = (Map<String, Object>) requestContext.get("http");
            final String httpRequestMethod = (String) httpInformation.get("method");
            builder.withMethod(httpRequestMethod);
            final String path = (String) httpInformation.get("path");
            builder.withPath(path);
            final Map<String, Object> headers = event.getMap("headers");
            final HeadersBuilder headersBuilder = headersBuilder();
            headers.forEach((key, value) -> headersBuilder.withAdditionalHeader(key, (String) value));
            builder.withHeaders(headersBuilder.build());
            final Map<String, String> queryParameters = Map.of();
            builder.withUniqueQueryParameters(queryParameters);
            final String body = "";
            builder.withBody(body);
            return builder.build();
        }, response -> {
            System.out.println("step -4");
            final int statusCode = response.status();
            System.out.println("step -3");
            final Map<String, List<String>> responseHeaders = response.headers();
            System.out.println("step -2");
            final String responseBody = response.stringBody();

            System.out.println("step -1");
            final LinkedHashMap<String, Object> responseMap = new LinkedHashMap<>();
            System.out.println("step 0");
            responseMap.put("statusCode", statusCode);

            System.out.println("step 1");
            final Map<String, String> singleHeaders = new LinkedHashMap<>();
            System.out.println("step 2");
            responseHeaders.forEach((key, values) -> singleHeaders.put(key, values.stream().collect(joining(","))));
            System.out.println("step 3");
            responseMap.put("headers", singleHeaders);
            //responseMap.put("headers", responseHeaders);
            System.out.println("responseBody = " + responseBody);
            System.out.println("step 4");
            responseMap.put("body", responseBody);
            System.out.println("step 5");

            /*
            System.out.println("starting to create response");
            try {
                final int statusCode = response.status();
                final Map<String, List<String>> responseHeaders = response.headers();
                final String responseBody = response.stringBody();

                final LinkedHashMap<String, Object> responseMap = new LinkedHashMap<>();
                responseMap.put("statusCode", statusCode);
                responseMap.put("multiValueHeaders", responseHeaders);
                final Map<String, String> singleHeaders = new LinkedHashMap<>();
                responseHeaders.forEach((key, values) -> singleHeaders.put(key, values.get(0)));
                responseMap.put("headers", singleHeaders);
                responseMap.put("body", responseBody);

                System.out.println("responseMap = " + responseMap);
                return responseMap;
            } catch (final Throwable e) {
                e.printStackTrace();
                throw e;
            }
             */

            return responseMap;
        });
    }

    private Map<String, Object> handleRestApiRequest(final AwsLambdaEvent event) {
        return httpMaid.handleRequestSynchronously(() -> {
            final RawHttpRequestBuilder builder = rawHttpRequestBuilder();
            final String httpRequestMethod = event.getAsString(HTTP_METHOD);
            builder.withMethod(httpRequestMethod);
            final String path = event.getAsString(PATH);
            builder.withPath(path);
            final Map<String, List<String>> headers = event.getOrDefault(MULTIVALUE_HEADERS, HashMap::new);
            final HeadersBuilder headersBuilder = HeadersBuilder.headersBuilder();
            headersBuilder.withHeadersMap(headers);
            builder.withHeaders(headersBuilder.build());

            final Map<String, List<String>> queryParameters = event.getOrDefault(QUERY_STRING_PARAMETERS, HashMap::new);
            final QueryParametersBuilder queryParametersBuilder = QueryParameters.builder();
            queryParameters.forEach(queryParametersBuilder::withParameter);
            builder.withQueryParameters(queryParametersBuilder.build());

            final String body = event.getOrDefault(BODY, "");
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
