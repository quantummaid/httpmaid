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

package de.quantummaid.httpmaid.client.issuer.bypass;

import de.quantummaid.httpmaid.HttpMaid;
import de.quantummaid.httpmaid.client.HttpClientRequest;
import de.quantummaid.httpmaid.client.RawClientResponse;
import de.quantummaid.httpmaid.client.RequestPath;
import de.quantummaid.httpmaid.client.UriString;
import de.quantummaid.httpmaid.client.issuer.Issuer;
import de.quantummaid.httpmaid.endpoint.RawRequestBuilder;
import de.quantummaid.httpmaid.util.streams.Streams;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.io.InputStream;
import java.util.Map;
import java.util.function.Function;

import static de.quantummaid.httpmaid.client.RawClientResponse.rawClientResponse;
import static de.quantummaid.httpmaid.endpoint.RawRequest.rawRequestBuilder;
import static de.quantummaid.httpmaid.util.Validators.validateNotNull;
import static java.util.stream.Collectors.toMap;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class BypassIssuer implements Issuer {
    private final HttpMaid httpMaid;

    public static Issuer bypassIssuer(final HttpMaid httpMaid) {
        validateNotNull(httpMaid, "httpMaid");
        return new BypassIssuer(httpMaid);
    }

    @Override
    public <T> T issue(final HttpClientRequest<T> request,
                       final Function<RawClientResponse, T> responseMapper) {
        final RequestPath requestPath = request.path();
        final Map<String, String> queryParameters = requestPath.queryParameters()
                .stream()
                .collect(toMap(
                        queryParameter -> queryParameter.key().encoded(),
                        queryParameter -> queryParameter.value().map(UriString::encoded).orElse(""))
                );
        final RawClientResponse rawClientResponse = httpMaid.handleRequestSynchronously(() -> {
            final RawRequestBuilder builder = rawRequestBuilder();
            builder.withPath(requestPath.path());
            builder.withMethod(request.method());
            builder.withUniqueHeaders(request.headers());
            builder.withQueryParameters(queryParameters);
            final InputStream body = request.body().orElseGet(() -> Streams.stringToInputStream(""));
            builder.withBody(body);
            return builder.build();
        }, response -> rawClientResponse(response.status(), response.uniqueHeaders(), response.body()));
        return responseMapper.apply(rawClientResponse);
    }

    @Override
    public void close() {
        // no resources to close
    }
}
