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

package de.quantummaid.httpmaid.spark;

import de.quantummaid.httpmaid.HttpMaid;
import de.quantummaid.httpmaid.endpoint.RawRequestBuilder;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import spark.Request;
import spark.Response;
import spark.Route;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static de.quantummaid.httpmaid.endpoint.RawRequest.rawRequestBuilder;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toSet;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
final class SparkRouteWebserviceAdapter implements Route {
    private final HttpMaid httpMaid;

    @Override
    public Object handle(final Request request, final Response sparkResponse) {
        httpMaid.handleRequest(() -> {
            final RawRequestBuilder builder = rawRequestBuilder();
            final String httpRequestMethod = request.requestMethod();
            builder.withMethod(httpRequestMethod);
            final String path = request.pathInfo();
            builder.withPath(path);
            builder.extractHeaders(request.headers(), request::headers);
            final Map<String, String> queryParameters = extractQueryParameters(request);
            builder.withQueryParameters(queryParameters);
            final InputStream body = request.raw().getInputStream();
            builder.withBody(body);
            return builder.build();
        }, response -> {
            response.setHeaders(sparkResponse::header);
            final int responseStatus = response.status();
            sparkResponse.status(responseStatus);
            final OutputStream outputStream = sparkResponse.raw().getOutputStream();
            response.streamBodyToOutputStream(outputStream);
        });
        return null;
    }

    private static Map<String, String> extractQueryParameters(final Request request) {
        final Set<String> queryParametersSet = request.queryParams();
        final Map<String, String> queryParameters = new HashMap<>();
        for (final String parameter : queryParametersSet) {
            final String[] values = request.queryParamsValues(parameter);
            final Set<String> valueSet = stream(values).collect(toSet());
            if (valueSet.isEmpty()) {
                queryParameters.put(parameter, null);
            } else {
                queryParameters.put(parameter, valueSet.iterator().next());
            }
        }
        return queryParameters;
    }
}
