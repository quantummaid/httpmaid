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
import de.quantummaid.httpmaid.http.Http;
import de.quantummaid.httpmaid.util.streams.Streams;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class StringBodyToStreamProcessor implements Processor {

    public static Processor stringBodyToStreamProcessor() {
        return new StringBodyToStreamProcessor();
    }

    @Override
    public void apply(final MetaData metaData) {
        metaData.getOptional(HttpMaidChainKeys.RESPONSE_CONTENT_TYPE)
                .ifPresent(contentType -> metaData.getOptional(HttpMaidChainKeys.RESPONSE_HEADERS)
                        .ifPresent(headers -> headers.put(Http.Headers.CONTENT_TYPE, contentType.internalValueForMapping())));
        if (!metaData.contains(HttpMaidChainKeys.RESPONSE_STREAM)) {
            metaData.getOptional(HttpMaidChainKeys.RESPONSE_BODY_STRING).ifPresent(stringResponse ->
                    metaData.set(HttpMaidChainKeys.RESPONSE_STREAM, Streams.stringToInputStream(stringResponse)));
        }
    }
}
