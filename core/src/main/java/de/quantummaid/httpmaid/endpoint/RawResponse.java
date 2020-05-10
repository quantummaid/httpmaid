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
import de.quantummaid.httpmaid.util.streams.Streams;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

import static de.quantummaid.httpmaid.HttpMaidChainKeys.*;
import static de.quantummaid.httpmaid.util.Validators.validateNotNull;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toMap;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class RawResponse {
    private final MetaData metaData;

    public static RawResponse rawResponse(final MetaData metaData) {
        validateNotNull(metaData, "metaData");
        return new RawResponse(metaData);
    }

    public int status() {
        return metaData.get(RESPONSE_STATUS);
    }

    public Map<String, List<String>> headers() {
        return uniqueHeaders().entrySet()
                .stream()
                .collect(toMap(
                        Map.Entry::getKey,
                        entry -> singletonList(entry.getValue()))
                );
    }

    public Map<String, String> uniqueHeaders() {
        return metaData.get(RESPONSE_HEADERS);
    }

    public void setHeaders(final BiConsumer<String, String> setter) {
        headers().forEach(
                (key, values) -> values.forEach(
                        value -> setter.accept(key, value)
                )
        );
    }

    public void streamBodyToOutputStream(final OutputStream outputStream) {
        final InputStream bodyStream = body();
        Streams.streamInputStreamToOutputStream(bodyStream, outputStream);
    }

    public InputStream body() {
        return metaData.getOptional(RESPONSE_STREAM)
                .orElseGet(() -> Streams.stringToInputStream(""));
    }

    public Optional<String> optionalStringBody() {
        return metaData.getOptional(RESPONSE_STREAM)
                .map(Streams::inputStreamToString);
    }

    public String stringBody() {
        return optionalStringBody()
                .orElse("");
    }
}
