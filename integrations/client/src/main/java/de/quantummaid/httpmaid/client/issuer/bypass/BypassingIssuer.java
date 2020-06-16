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
import de.quantummaid.httpmaid.client.*;
import de.quantummaid.httpmaid.client.issuer.Issuer;
import de.quantummaid.httpmaid.endpoint.RawHttpRequestBuilder;
import de.quantummaid.httpmaid.http.HeadersBuilder;
import de.quantummaid.httpmaid.http.QueryParameters;
import de.quantummaid.httpmaid.http.QueryParametersBuilder;
import de.quantummaid.httpmaid.util.streams.Streams;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.io.InputStream;
import java.util.function.Function;

import static de.quantummaid.httpmaid.client.RawClientResponse.rawClientResponse;
import static de.quantummaid.httpmaid.endpoint.RawHttpRequest.rawHttpRequestBuilder;
import static de.quantummaid.httpmaid.http.HeadersBuilder.headersBuilder;
import static de.quantummaid.httpmaid.util.Validators.validateNotNull;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class BypassingIssuer implements Issuer {
    private final HttpMaid httpMaid;

    public static Issuer bypassIssuer(final HttpMaid httpMaid) {
        validateNotNull(httpMaid, "httpMaid");
        return new BypassingIssuer(httpMaid);
    }

    @Override
    public <T> T issue(final HttpClientRequest<T> request,
                       final Function<RawClientResponse, T> responseMapper,
                       final BasePath basePath) {
        final RequestPath requestPath = request.path(basePath);
        final QueryParametersBuilder queryParametersBuilder = QueryParameters.builder();
        requestPath.queryParameters()
                .forEach(queryParameter -> queryParametersBuilder.withParameter(
                        queryParameter.key()
                                .unencoded(),
                        queryParameter.value()
                                .map(UriString::unencoded)
                                .orElse(""))
                );
        final RawClientResponse rawClientResponse = httpMaid.handleRequestSynchronously(() -> {
            final RawHttpRequestBuilder builder = rawHttpRequestBuilder();
            builder.withPath(requestPath.unencodedPath());
            builder.withMethod(request.method());

            final HeadersBuilder headersBuilder = headersBuilder();
            request.headers().forEach(header -> headersBuilder.withAdditionalHeader(header.name(), header.value()));
            builder.withHeaders(headersBuilder.build());
            builder.withQueryParameters(queryParametersBuilder.build());
            final InputStream body = request.body().orElseGet(() -> Streams.stringToInputStream(""));
            builder.withBody(body);
            return builder.build();
        }, response -> rawClientResponse(response.status(), response.headers(), response.body()));
        return responseMapper.apply(rawClientResponse);
    }

    @Override
    public void close() {
        // no resources to close
    }
}
