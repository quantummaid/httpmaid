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

package de.quantummaid.httpmaid.cors.domain;

import de.quantummaid.httpmaid.chains.MetaData;
import de.quantummaid.httpmaid.cors.Cors;
import de.quantummaid.httpmaid.util.Validators;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.List;
import java.util.Optional;

import static de.quantummaid.httpmaid.HttpMaidChainKeys.REQUEST_HEADERS;
import static java.util.Arrays.stream;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class RequestedHeaders {
    private final List<RequestedHeader> headers;

    public static RequestedHeaders load(final MetaData metaData) {
        Validators.validateNotNull(metaData, "metaData");
        final String commaSeparatedHeaders = metaData.get(REQUEST_HEADERS)
                .getOptionalHeader(Cors.ACCESS_CONTROL_REQUEST_HEADERS).orElse("");
        Validators.validateNotNull(commaSeparatedHeaders, "commaSeparatedHeaders");
        final List<RequestedHeader> headers = stream(commaSeparatedHeaders.split(","))
                .filter(s -> !s.isEmpty())
                .map(RequestedHeader::requestedHeader)
                .collect(toList());
        return new RequestedHeaders(headers);
    }

    public Optional<String> generateHeaderValue() {
        if(hasOnlySimpleHeaders()) {
            return empty();
        }
        return of(headers.stream()
                .filter(header -> !header.isSimpleHeader())
                .map(RequestedHeader::internalValueForMapping)
                .collect(joining(",")));
    }

    private boolean hasOnlySimpleHeaders() {
        for (final RequestedHeader header : headers) {
            if(!header.isSimpleHeader()) {
                return false;
            }
        }
        return true;
    }

    public List<RequestedHeader> headers() {
        return headers;
    }
}
