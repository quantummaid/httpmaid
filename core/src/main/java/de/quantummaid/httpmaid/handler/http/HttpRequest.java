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

package de.quantummaid.httpmaid.handler.http;

import de.quantummaid.httpmaid.chains.MetaData;
import de.quantummaid.httpmaid.http.Headers;
import de.quantummaid.httpmaid.http.HttpRequestMethod;
import de.quantummaid.httpmaid.http.PathParameters;
import de.quantummaid.httpmaid.http.QueryParameters;
import de.quantummaid.httpmaid.http.headers.cookies.Cookies;
import de.quantummaid.httpmaid.path.Path;
import lombok.*;

import java.io.InputStream;
import java.util.Map;
import java.util.Optional;

import static de.quantummaid.httpmaid.HttpMaidChainKeys.*;
import static de.quantummaid.httpmaid.http.headers.cookies.Cookies.cookiesFromHeaders;
import static de.quantummaid.httpmaid.util.Validators.validateNotNull;
import static java.lang.String.format;

@Getter
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class HttpRequest {
    private final MetaData metaData;

    public static HttpRequest httpRequest(final MetaData metaData) {
        validateNotNull(metaData, "metaData");
        return new HttpRequest(metaData);
    }

    public HttpRequestMethod method() {
        return metaData.get(METHOD);
    }

    public Path path() {
        return metaData.get(PATH);
    }

    public PathParameters pathParameters() {
        return metaData.get(PATH_PARAMETERS);
    }

    public QueryParameters queryParameters() {
        return metaData.get(QUERY_PARAMETERS);
    }

    public Headers headers() {
        return metaData.get(REQUEST_HEADERS);
    }

    public Cookies cookies() {
        return cookiesFromHeaders(headers());
    }

    public Optional<String> optionalBodyString() {
        return metaData.getOptional(REQUEST_BODY_STRING);
    }

    public String bodyString() {
        return optionalBodyString()
                .orElseThrow(() -> new RuntimeException("Request does not have a body"));
    }

    public InputStream bodyStream() {
        return metaData.get(REQUEST_BODY_STREAM);
    }

    @SuppressWarnings("unchecked")
    public <T> T authenticationInformationAs(final Class<T> type) {
        return (T) authenticationInformation();
    }

    public Object authenticationInformation() {
        return optionalAuthenticationInformation()
                .orElseThrow(() -> new RuntimeException("Request is not authenticated"));
    }

    public Optional<Object> optionalAuthenticationInformation() {
        return metaData.getOptional(AUTHENTICATION_INFORMATION);
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<T> optionalAuthenticationInformationAs(final Class<T> type) {
        validateNotNull(type, "type");
        return (Optional<T>) optionalAuthenticationInformation();
    }

    public Map<String, Object> bodyMap() {
        return optionalBodyMap()
                .orElseThrow(() -> new RuntimeException(format("Request does not have a body map.%n%n%s", metaData.prettyPrint())));
    }

    @SuppressWarnings("unchecked")
    public Optional<Map<String, Object>> optionalBodyMap() {
        return metaData.getOptional(UNMARSHALLED_REQUEST_BODY)
                .flatMap(unmarshalled -> {
                    if (unmarshalled instanceof Map) {
                        return Optional.of((Map<String, Object>) unmarshalled);
                    } else {
                        return Optional.empty();
                    }
                });
    }
}
