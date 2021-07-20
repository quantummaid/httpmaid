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

package de.quantummaid.httpmaid.marshalling.processors;

import de.quantummaid.httpmaid.chains.MetaData;
import de.quantummaid.httpmaid.chains.Processor;
import de.quantummaid.httpmaid.handler.http.HttpRequest;
import de.quantummaid.httpmaid.http.headers.ContentType;
import de.quantummaid.httpmaid.marshalling.DefaultContentTypeProvider;
import de.quantummaid.httpmaid.marshalling.Unmarshaller;
import de.quantummaid.httpmaid.marshalling.Unmarshallers;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static de.quantummaid.httpmaid.HttpMaidChainKeys.*;
import static de.quantummaid.httpmaid.handler.http.HttpRequest.httpRequest;
import static de.quantummaid.httpmaid.marshalling.UnsupportedContentTypeException.unsupportedContentTypeException;
import static de.quantummaid.httpmaid.util.Validators.validateNotNull;
import static java.util.Objects.nonNull;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class UnmarshalProcessor implements Processor {
    private static final Logger LOGGER = LoggerFactory.getLogger(UnmarshalProcessor.class);

    private final Unmarshallers unmarshallersMap;
    private final boolean throwExceptionIfNoMarshallerFound;
    private final DefaultContentTypeProvider defaultContentTypeProvider;

    public static UnmarshalProcessor unmarshalProcessor(
            final Unmarshallers unmarshallers,
            final boolean throwExceptionIfNoMarshallerFound,
            final DefaultContentTypeProvider defaultContentTypeProvider) {
        validateNotNull(unmarshallers, "unmarshallers");
        validateNotNull(defaultContentTypeProvider, "defaultContentTypeProvider");
        return new UnmarshalProcessor(
                unmarshallers,
                throwExceptionIfNoMarshallerFound,
                defaultContentTypeProvider
        );
    }

    @Override
    public void apply(final MetaData metaData) {
        metaData.getOptional(REQUEST_BODY_STRING).ifPresent(body -> {
            final ContentType contentType = metaData.get(REQUEST_CONTENT_TYPE);
            final Unmarshaller unmarshaller;
            if (contentType.isEmpty()) {
                unmarshaller = defaultUnmarshaller(metaData);
            } else if (unmarshallersMap.supportsContentType(contentType)) {
                unmarshaller = unmarshallersMap.byContentType(contentType);
            } else if (!throwExceptionIfNoMarshallerFound) {
                unmarshaller = defaultUnmarshaller(metaData);
            } else {
                throw unsupportedContentTypeException(contentType, unmarshallersMap.supportedContentTypes());
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
        final ContentType defaultContentType = defaultContentTypeProvider.provideDefaultContentType(request);
        return unmarshallersMap.byContentType(defaultContentType);
    }
}
