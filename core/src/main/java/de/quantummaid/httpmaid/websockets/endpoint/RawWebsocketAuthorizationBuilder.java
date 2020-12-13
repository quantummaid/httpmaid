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

package de.quantummaid.httpmaid.websockets.endpoint;

import de.quantummaid.httpmaid.chains.MetaDataKey;
import de.quantummaid.httpmaid.http.Headers;
import de.quantummaid.httpmaid.http.QueryParameters;
import de.quantummaid.httpmaid.http.QueryParametersBuilder;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static de.quantummaid.httpmaid.websockets.endpoint.RawWebsocketAuthorization.rawWebsocketAuthorization;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class RawWebsocketAuthorizationBuilder {
    private Headers headers;
    private QueryParameters queryParameters;
    private final Map<MetaDataKey<?>, Object> additionalMetaData = new LinkedHashMap<>();

    public static RawWebsocketAuthorizationBuilder rawWebsocketAuthorizationBuilder() {
        return new RawWebsocketAuthorizationBuilder();
    }

    public RawWebsocketAuthorizationBuilder withHeaders(final Headers headers) {
        this.headers = headers;
        return this;
    }

    public RawWebsocketAuthorizationBuilder withEncodedQueryString(final String encodedQueryParameters) {
        return withQueryParameters(QueryParameters.fromQueryString(encodedQueryParameters));
    }

    public RawWebsocketAuthorizationBuilder withQueryParameters(final QueryParameters queryParameters) {
        this.queryParameters = queryParameters;
        return this;
    }

    public RawWebsocketAuthorizationBuilder withQueryParameterMap(final Map<String, List<String>> queryParameters) {
        final QueryParametersBuilder builder = QueryParameters.builder();
        queryParameters.forEach(builder::withParameter);
        this.queryParameters = builder.build();
        return this;
    }

    public <T> RawWebsocketAuthorizationBuilder withAdditionalMetaData(final MetaDataKey<T> key, final T value) {
        additionalMetaData.put(key, value);
        return this;
    }

    public RawWebsocketAuthorization build() {
        return rawWebsocketAuthorization(queryParameters, headers, additionalMetaData);
    }
}
