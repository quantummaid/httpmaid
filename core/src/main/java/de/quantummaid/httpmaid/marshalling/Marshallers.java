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

package de.quantummaid.httpmaid.marshalling;

import de.quantummaid.httpmaid.chains.MetaData;
import de.quantummaid.httpmaid.chains.MetaDataKey;
import de.quantummaid.httpmaid.handler.http.HttpRequest;
import de.quantummaid.httpmaid.http.headers.ContentType;
import de.quantummaid.httpmaid.http.headers.accept.Accept;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static de.quantummaid.httpmaid.HttpMaidChainKeys.REQUEST_CONTENT_TYPE;
import static de.quantummaid.httpmaid.HttpMaidChainKeys.RESPONSE_HEADERS;
import static de.quantummaid.httpmaid.chains.MetaDataKey.metaDataKey;
import static de.quantummaid.httpmaid.handler.http.HttpRequest.httpRequest;
import static de.quantummaid.httpmaid.http.Http.Headers.CONTENT_TYPE;
import static de.quantummaid.httpmaid.http.headers.accept.Accept.fromMetaData;
import static de.quantummaid.httpmaid.marshalling.UnsupportedContentTypeException.unsupportedContentTypeException;
import static java.util.Objects.isNull;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@SuppressWarnings("java:S1845")
public final class Marshallers {
    public static final MetaDataKey<Marshallers> MARSHALLERS = metaDataKey("MARSHALLERS");

    private final Map<ContentType, Marshaller> marshallersMap;
    private final DefaultContentTypeProvider defaultContentTypeProvider;

    public static Marshallers marshallers(final Map<ContentType, Marshaller> marshallers,
                                          final DefaultContentTypeProvider defaultContentTypeProvider) {
        return new Marshallers(marshallers, defaultContentTypeProvider);
    }

    public boolean isEmpty() {
        return marshallersMap.isEmpty();
    }

    public Marshaller marshallerFor(final ContentType responseContentType) {
        final Marshaller marshaller = marshallersMap.get(responseContentType);
        if (isNull(marshaller)) {
            throw unsupportedContentTypeException(responseContentType, marshallersMap.keySet());
        }
        return marshaller;
    }

    public Marshaller determineResponseMarshaller(final MetaData metaData) {
        final ContentType responseContentType = determineResponseContentType(metaData);
        return marshallerFor(responseContentType);
    }

    public ContentType determineResponseContentType(final MetaData metaData) {
        final Optional<ContentType> responseContentType = responseContentType(metaData);
        if (responseContentType.isPresent()) {
            return responseContentType.get();
        }
        final Accept accept = fromMetaData(metaData);
        final List<ContentType> candidates = marshallersMap.keySet().stream()
                .filter(accept::contentTypeIsAccepted)
                .collect(toList());
        if (candidates.isEmpty()) {
            return defaultResponseContentType(metaData)
                    .orElseThrow(() -> ResponseContentTypeCouldNotBeDeterminedException.responseContentTypeCouldNotBeDeterminedException(metaData));
        }
        final Optional<ContentType> requestContentType = metaData.getOptional(REQUEST_CONTENT_TYPE)
                .filter(candidates::contains);
        if (requestContentType.isPresent()) {
            return requestContentType.get();
        }
        final HttpRequest request = httpRequest(metaData);
        final ContentType defaultContentType = this.defaultContentTypeProvider.provideDefaultContentType(request);
        if (candidates.contains(defaultContentType)) {
            return defaultContentType;
        }
        return candidates.get(0);
    }

    private Optional<ContentType> defaultResponseContentType(final MetaData metaData) {
        if (marshallersMap.isEmpty()) {
            return empty();
        }
        final HttpRequest request = httpRequest(metaData);
        final ContentType defaultContentType = this.defaultContentTypeProvider.provideDefaultContentType(request);
        if (marshallersMap.containsKey(defaultContentType)) {
            return ofNullable(defaultContentType);
        }
        return ofNullable(marshallersMap.keySet().iterator().next());
    }

    private static Optional<ContentType> responseContentType(final MetaData metaData) {
        return metaData.getOptional(RESPONSE_HEADERS).flatMap(headers ->
                headers.getOptionalHeader(CONTENT_TYPE).map(ContentType::fromString));
    }
}
