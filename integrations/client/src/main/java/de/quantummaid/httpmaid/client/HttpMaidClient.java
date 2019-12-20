/*
 * Copyright (c) 2019 Richard Hauswald - https://quantummaid.de/.
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

package de.quantummaid.httpmaid.client;

import de.quantummaid.httpmaid.client.issuer.Issuer;
import de.quantummaid.httpmaid.client.issuer.real.RealIssuer;
import de.quantummaid.httpmaid.HttpMaid;
import de.quantummaid.httpmaid.client.clientbuilder.PortStage;
import de.quantummaid.httpmaid.filtermap.FilterMap;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import static de.quantummaid.httpmaid.client.issuer.bypass.BypassIssuer.bypassIssuer;
import static de.quantummaid.httpmaid.util.Validators.validateNotNull;
import static de.quantummaid.httpmaid.util.Validators.validateNotNullNorEmpty;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class HttpMaidClient implements AutoCloseable {
    private final Issuer issuer;
    private final BasePath basePath;
    private final FilterMap<Class<?>, ClientResponseMapper<?>> responseMappers;

    static HttpMaidClient httpMaidClient(final Issuer issuer,
                                         final BasePath basePath,
                                         final FilterMap<Class<?>, ClientResponseMapper<?>> responseMappers) {
        validateNotNull(issuer, "issuer");
        validateNotNull(basePath, "basePath");
        validateNotNull(responseMappers, "responseMappers");
        return new HttpMaidClient(issuer, basePath, responseMappers);
    }

    public static HttpMaidClientBuilder aHttpMaidClientBypassingRequestsDirectlyTo(final HttpMaid httpMaid) {
        validateNotNull(httpMaid, "httpMaid");
        final Issuer issuer = bypassIssuer(httpMaid);
        return HttpMaidClientBuilder.clientBuilder(basePath -> issuer);
    }

    public static PortStage aHttpMaidClientForTheHost(final String host) {
        validateNotNullNorEmpty(host, "host");
        return port -> protocol -> {
            validateNotNull(protocol, "protocol");
            return HttpMaidClientBuilder.clientBuilder(basePath -> RealIssuer.realIssuer(protocol, host, port, basePath));
        };
    }

    public static PortStage aHttpMaidClientThatReusesConnectionsForTheHost(final String host) {
        validateNotNullNorEmpty(host, "host");
        return port -> protocol -> {
            validateNotNull(protocol, "protocol");
            return HttpMaidClientBuilder.clientBuilder(basePath -> RealIssuer.realIssuerWithConnectionReuse(protocol, host, port, basePath));
        };
    }

    public <T> T issue(final HttpClientRequestBuilder<T> requestBuilder) {
        return issue(requestBuilder.build(basePath));
    }

    @SuppressWarnings("unchecked")
    public <T> T issue(final HttpClientRequest<T> request) {
        validateNotNull(request, "request");
        return issuer.issue(request, response -> {
            final Class<T> targetType = request.targetType();
            final ClientResponseMapper<T> responseMapper = (ClientResponseMapper<T>) responseMappers.get(targetType);
            return responseMapper.map(response, targetType);
        });
    }

    @Override
    public void close() {
        issuer.close();
    }
}
