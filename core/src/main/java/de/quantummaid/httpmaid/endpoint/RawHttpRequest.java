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

package de.quantummaid.httpmaid.endpoint;

import de.quantummaid.httpmaid.chains.MetaData;
import de.quantummaid.httpmaid.chains.MetaDataKey;
import de.quantummaid.httpmaid.http.Headers;
import de.quantummaid.httpmaid.http.QueryParameters;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.io.InputStream;
import java.util.Map;

import static de.quantummaid.httpmaid.HttpMaidChainKeys.*;
import static de.quantummaid.httpmaid.util.Validators.validateNotNull;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class RawHttpRequest implements RawRequest {
    private final String path;
    private final String requestMethod;
    private final Headers headers;
    private final QueryParameters queryParameters;
    private final Map<String, String> queryParameterMap;
    private final InputStream body;
    private final Map<MetaDataKey<?>, Object> additionalMetaData;

    public static RawHttpRequestBuilder rawHttpRequestBuilder() {
        return RawHttpRequestBuilder.rawHttpRequestBuilder();
    }

    public static RawHttpRequest rawHttpRequest(final String path,
                                                final String requestMethod,
                                                final Headers headers,
                                                final QueryParameters queryParameters,
                                                final InputStream body,
                                                final Map<MetaDataKey<?>, Object> additionalMetaData) {
        validateNotNull(path, "path");
        validateNotNull(requestMethod, "requestMethod");
        validateNotNull(headers, "headers");
        validateNotNull(queryParameters, "queryParameters");
        validateNotNull(body, "body");
        return new RawHttpRequest(path, requestMethod, headers, queryParameters, null, body, additionalMetaData);
    }

    public static RawHttpRequest rawHttpRequest(final String path,
                                                final String requestMethod,
                                                final Headers headers,
                                                final Map<String, String> queryParameters,
                                                final InputStream body,
                                                final Map<MetaDataKey<?>, Object> additionalMetaData) {
        validateNotNull(path, "path");
        validateNotNull(requestMethod, "requestMethod");
        validateNotNull(headers, "headers");
        validateNotNull(queryParameters, "queryParameters");
        validateNotNull(body, "body");
        return new RawHttpRequest(path, requestMethod, headers, null, queryParameters, body, additionalMetaData);
    }

    @Override
    public void enter(final MetaData metaData) {
        metaData.set(RAW_PATH, path);
        metaData.set(RAW_METHOD, requestMethod);
        metaData.set(REQUEST_HEADERS, headers);
        metaData.set(QUERY_PARAMETERS, queryParameters);
        metaData.set(REQUEST_BODY_STREAM, body);
        metaData.set(IS_HTTP_REQUEST, true);
        additionalMetaData.forEach(metaData::setUnchecked);
    }
}
