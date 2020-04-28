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

package de.quantummaid.httpmaid.client;

import de.quantummaid.httpmaid.client.issuer.Issuer;
import de.quantummaid.httpmaid.filtermap.FilterMapBuilder;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.function.Function;
import java.util.function.Predicate;

import static de.quantummaid.httpmaid.client.BasePath.basePath;
import static de.quantummaid.httpmaid.client.HttpMaidClient.httpMaidClient;
import static de.quantummaid.httpmaid.client.HttpMaidClientException.httpMaidClientException;
import static de.quantummaid.httpmaid.client.SimpleHttpResponseObject.httpClientResponse;
import static de.quantummaid.httpmaid.client.UnsupportedTargetTypeException.unsupportedTargetTypeException;
import static de.quantummaid.httpmaid.filtermap.FilterMapBuilder.filterMapBuilder;
import static de.quantummaid.httpmaid.util.Validators.validateNotNull;
import static de.quantummaid.httpmaid.util.streams.Streams.inputStreamToString;
import static java.lang.String.format;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class HttpMaidClientBuilder {
    private final Function<BasePath, Issuer> issuerFactory;
    private final FilterMapBuilder<Class<?>, ClientResponseMapper<?>> responseMappers;
    private BasePath basePath = basePath("");

    static HttpMaidClientBuilder clientBuilder(final Function<BasePath, Issuer> issuer) {
        validateNotNull(issuer, "issuer");
        final HttpMaidClientBuilder builder = new HttpMaidClientBuilder(issuer, filterMapBuilder());
        builder.withResponseMapping(String.class, (response, targetType) -> {
            if (targetType.equals(String.class)) {
                return inputStreamToString(response.content());
            }
            throw unsupportedTargetTypeException(String.class, targetType);
        });
        builder.withResponseMapping(SimpleHttpResponseObject.class, (response, targetType) -> {
            if (targetType.equals(SimpleHttpResponseObject.class)) {
                final String body = inputStreamToString(response.content());
                return httpClientResponse(response.statusCode(), response.headers(), body);
            }
            throw unsupportedTargetTypeException(SimpleHttpResponseObject.class, targetType);
        });
        builder.withDefaultResponseMapping((response, targetType) -> {
            throw httpMaidClientException(format("Cannot map response '%s' to type '%s' because no default " +
                            "response mapper was defined", response.toString(), targetType.getName()));
        });
        return builder;
    }

    public HttpMaidClientBuilder withBasePath(final String basePath) {
        this.basePath = basePath(basePath);
        return this;
    }

    public HttpMaidClientBuilder withDefaultResponseMapping(final ClientResponseMapper<?> mapper) {
        validateNotNull(mapper, "mapper"); // NOSONAR
        responseMappers.setDefaultValue(mapper);
        return this;
    }

    public <T> HttpMaidClientBuilder withResponseMapping(final Class<T> type,
                                                         final ClientResponseMapper<T> mapper) {
        validateNotNull(type, "type");
        validateNotNull(mapper, "mapper"); // NOSONAR
        return withResponseMapping(subtype(type), mapper);
    }

    @SuppressWarnings("unchecked")
    public <T> HttpMaidClientBuilder withResponseMapping(final Predicate<Class<T>> filter,
                                                         final ClientResponseMapper<T> mapper) {
        validateNotNull(filter, "filter");
        validateNotNull(mapper, "mapper"); // NOSONAR
        responseMappers.put((Predicate<Class<?>>) (Object) filter, mapper);
        return this;
    }

    public HttpMaidClient build() {
        final Issuer issuer = this.issuerFactory.apply(basePath);
        return httpMaidClient(issuer, basePath, responseMappers.build());
    }

    private static <T> Predicate<Class<T>> subtype(final Class<T> type) {
        return type::isAssignableFrom;
    }
}
