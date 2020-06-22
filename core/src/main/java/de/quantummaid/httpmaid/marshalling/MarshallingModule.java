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

import de.quantummaid.httpmaid.chains.ChainExtender;
import de.quantummaid.httpmaid.chains.ChainModule;
import de.quantummaid.httpmaid.chains.MetaData;
import de.quantummaid.httpmaid.handler.http.HttpRequest;
import de.quantummaid.httpmaid.http.headers.ContentType;
import de.quantummaid.httpmaid.http.headers.accept.Accept;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import static de.quantummaid.httpmaid.HttpMaidChainKeys.*;
import static de.quantummaid.httpmaid.HttpMaidChains.POST_INVOKE;
import static de.quantummaid.httpmaid.HttpMaidChains.PROCESS_BODY_STRING;
import static de.quantummaid.httpmaid.handler.http.HttpRequest.httpRequest;
import static de.quantummaid.httpmaid.http.Http.Headers.CONTENT_TYPE;
import static de.quantummaid.httpmaid.http.headers.accept.Accept.fromMetaData;
import static de.quantummaid.httpmaid.marshalling.UnsupportedContentTypeException.unsupportedContentTypeException;
import static de.quantummaid.httpmaid.util.Validators.validateNotNull;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class MarshallingModule implements ChainModule {
    private static final Logger LOGGER = LoggerFactory.getLogger(MarshallingModule.class);

    private DefaultContentTypeProvider defaultContentTypeProvider;
    private final Map<ContentType, Unmarshaller> unmarshallers;
    private final Map<ContentType, Marshaller> marshallers;
    private boolean throwExceptionIfNoMarshallerFound;

    public static MarshallingModule emptyMarshallingModule() {
        return new MarshallingModule(new HashMap<>(), new HashMap<>());
    }

    public void addUnmarshaller(final ContentType contentType, final Unmarshaller unmarshaller) {
        validateNotNull(contentType, "contentType");
        validateNotNull(unmarshaller, "unmarshaller");
        unmarshallers.put(contentType, unmarshaller);
        if (defaultContentTypeProvider == null) {
            setDefaultContentTypeProvider(contentType);
        }
    }

    public void addMarshaller(final ContentType contentType, final Marshaller marshaller) {
        validateNotNull(contentType, "contentType");
        validateNotNull(marshaller, "marshaller");
        marshallers.put(contentType, marshaller);
        if (defaultContentTypeProvider == null) {
            setDefaultContentTypeProvider(contentType);
        }
    }

    public void setDefaultContentTypeProvider(final ContentType defaultContentTypeProvider) {
        validateNotNull(defaultContentTypeProvider, "defaultContentType");
        setDefaultContentTypeProvider(request -> defaultContentTypeProvider);
    }

    public void setDefaultContentTypeProvider(final DefaultContentTypeProvider defaultContentType) {
        validateNotNull(defaultContentType, "defaultContentType");
        this.defaultContentTypeProvider = defaultContentType;
    }

    public void setThrowExceptionIfNoMarshallerFound(final boolean throwExceptionIfNoMarshallerFound) {
        this.throwExceptionIfNoMarshallerFound = throwExceptionIfNoMarshallerFound;
    }

    @Override
    public void register(final ChainExtender extender) {
        extender.appendProcessor(PROCESS_BODY_STRING, this::processUnmarshalling);
        extender.prependProcessor(POST_INVOKE, this::processMarshalling);
    }

    private void processUnmarshalling(final MetaData metaData) {
        metaData.getOptional(REQUEST_BODY_STRING).ifPresent(body -> {
            final ContentType contentType = metaData.get(REQUEST_CONTENT_TYPE);
            final Unmarshaller unmarshaller;
            if (contentType.isEmpty()) {
                unmarshaller = defaultUnmarshaller(metaData);
            } else if (unmarshallers.containsKey(contentType)) {
                unmarshaller = unmarshallers.get(contentType);
            } else if (!throwExceptionIfNoMarshallerFound) {
                unmarshaller = defaultUnmarshaller(metaData);
            } else {
                throw unsupportedContentTypeException(contentType, unmarshallers.keySet());
            }
            if (nonNull(unmarshaller)) {
                try {
                    final Object unmarshalled = unmarshaller.unmarshall(body);
                    metaData.set(UNMARSHALLED_REQUEST_BODY, unmarshalled);
                } catch (final Exception e) {
                    LOGGER.info("exception during marshalling", e);
                }
            }
        });
    }

    private Unmarshaller defaultUnmarshaller(final MetaData metaData) {
        final HttpRequest request = httpRequest(metaData);
        final ContentType defaultContentType = this.defaultContentTypeProvider.provideDefaultContentType(request);
        return unmarshallers.get(defaultContentType);
    }

    private void processMarshalling(final MetaData metaData) {
        try {
            metaData.getOptional(RESPONSE_BODY_OBJECT).ifPresent(map -> {
                final ContentType responseContentType = determineResponseContentType(metaData);
                final Marshaller marshaller = marshallerFor(responseContentType);
                metaData.set(RESPONSE_CONTENT_TYPE, responseContentType);
                final String stringBody = marshaller.marshall(map);
                metaData.set(RESPONSE_BODY_STRING, stringBody);
            });

        } catch (final MarshallingException e) {
            if (metaData.getOptional(EXCEPTION).isEmpty()) {
                failIfConfiguredToDoSo(() -> MarshallingException.marshallingException(e));
            }
        }
    }

    private Marshaller marshallerFor(final ContentType responseContentType) {
        final Marshaller marshaller = marshallers.get(responseContentType);
        if (isNull(marshaller)) {
            throw unsupportedContentTypeException(responseContentType, marshallers.keySet());
        }
        return marshaller;
    }

    private ContentType determineResponseContentType(final MetaData metaData) {
        final Optional<ContentType> responseContentType = responseContentType(metaData);
        if (responseContentType.isPresent()) {
            return responseContentType.get();
        }
        final Accept accept = fromMetaData(metaData);
        final List<ContentType> candidates = marshallers.keySet().stream()
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
        if (marshallers.isEmpty()) {
            return empty();
        }
        final HttpRequest request = httpRequest(metaData);
        final ContentType defaultContentType = this.defaultContentTypeProvider.provideDefaultContentType(request);
        if (marshallers.containsKey(defaultContentType)) {
            return ofNullable(defaultContentType);
        }
        return ofNullable(marshallers.keySet().iterator().next());
    }

    private static Optional<ContentType> responseContentType(final MetaData metaData) {
        return metaData.getOptional(RESPONSE_HEADERS).flatMap(headers ->
                headers.getOptionalHeader(CONTENT_TYPE).map(ContentType::fromString));
    }

    private void failIfConfiguredToDoSo(final Supplier<RuntimeException> exceptionSupplier) {
        if (throwExceptionIfNoMarshallerFound) {
            throw exceptionSupplier.get();
        }
    }
}
