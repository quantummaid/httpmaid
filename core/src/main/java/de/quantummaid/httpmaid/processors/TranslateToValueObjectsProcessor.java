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

import de.quantummaid.httpmaid.chains.MetaData;
import de.quantummaid.httpmaid.chains.Processor;
import de.quantummaid.httpmaid.http.Headers;
import de.quantummaid.httpmaid.http.HttpRequestMethod;
import de.quantummaid.httpmaid.http.QueryParameters;
import de.quantummaid.httpmaid.http.headers.ContentType;
import de.quantummaid.httpmaid.path.Path;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Optional;

import static de.quantummaid.httpmaid.HttpMaidChainKeys.*;
import static de.quantummaid.httpmaid.http.Http.Headers.CONTENT_TYPE;
import static java.util.Optional.empty;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class TranslateToValueObjectsProcessor implements Processor {

    public static Processor translateToValueObjectsProcessor() {
        return new TranslateToValueObjectsProcessor();
    }

    @Override
    public void apply(final MetaData metaData) {
        metaData.getOptional(RAW_REQUEST_HEADERS).ifPresentOrElse(rawHeaders -> {
                    final Headers headers = Headers.headers(rawHeaders);
                    metaData.set(REQUEST_HEADERS, headers);
                    final Optional<String> optionalContentType = headers.getOptionalHeader(CONTENT_TYPE);
                    final ContentType contentType = ContentType.fromString(optionalContentType);
                    metaData.set(REQUEST_CONTENT_TYPE, contentType);
                },
                () -> metaData.set(REQUEST_CONTENT_TYPE, ContentType.fromString(empty()))
        );

        metaData.getOptional(RAW_REQUEST_QUERY_PARAMETERS).ifPresent(rawQueryParameters -> {
            final QueryParameters queryParameters = QueryParameters.queryParameters(rawQueryParameters);
            metaData.set(QUERY_PARAMETERS, queryParameters);
        });

        metaData.getOptional(RAW_METHOD).ifPresent(rawMethod -> metaData.set(METHOD, HttpRequestMethod.parse(rawMethod)));
        metaData.getOptional(RAW_PATH).ifPresent(rawPath -> metaData.set(PATH, Path.path(rawPath)));
    }
}
