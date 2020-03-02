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

package de.quantummaid.httpmaid.processors;

import de.quantummaid.httpmaid.HttpMaidChainKeys;
import de.quantummaid.httpmaid.chains.MetaData;
import de.quantummaid.httpmaid.chains.Processor;
import de.quantummaid.httpmaid.http.Headers;
import de.quantummaid.httpmaid.http.Http;
import de.quantummaid.httpmaid.http.HttpRequestMethod;
import de.quantummaid.httpmaid.http.QueryParameters;
import de.quantummaid.httpmaid.http.headers.ContentType;
import de.quantummaid.httpmaid.path.Path;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.List;
import java.util.Map;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class TranslateToValueObjectsProcessor implements Processor {

    public static Processor translateToValueObjectsProcessor() {
        return new TranslateToValueObjectsProcessor();
    }

    @Override
    public void apply(final MetaData metaData) {
        final Map<String, List<String>> rawHeaders = metaData.get(HttpMaidChainKeys.RAW_REQUEST_HEADERS);
        final Headers headers = Headers.headers(rawHeaders);
        metaData.set(HttpMaidChainKeys.REQUEST_HEADERS, headers);
        final ContentType contentType = ContentType.fromString(headers.getOptionalHeader(Http.Headers.CONTENT_TYPE));
        metaData.set(HttpMaidChainKeys.REQUEST_CONTENT_TYPE, contentType);

        final Map<String, String> rawQueryParameters = metaData.get(HttpMaidChainKeys.RAW_REQUEST_QUERY_PARAMETERS);
        final QueryParameters queryParameters = QueryParameters.queryParameters(rawQueryParameters);
        metaData.set(HttpMaidChainKeys.QUERY_PARAMETERS, queryParameters);

        final String rawMethod = metaData.get(HttpMaidChainKeys.RAW_METHOD);
        metaData.set(HttpMaidChainKeys.METHOD, HttpRequestMethod.parse(rawMethod));

        final String rawPath = metaData.get(HttpMaidChainKeys.RAW_PATH);
        metaData.set(HttpMaidChainKeys.PATH, Path.path(rawPath));
    }
}
