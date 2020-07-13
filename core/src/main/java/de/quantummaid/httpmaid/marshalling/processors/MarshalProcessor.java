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
import de.quantummaid.httpmaid.http.headers.ContentType;
import de.quantummaid.httpmaid.marshalling.Marshaller;
import de.quantummaid.httpmaid.marshalling.Marshallers;
import de.quantummaid.httpmaid.marshalling.MarshallingException;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.function.Supplier;

import static de.quantummaid.httpmaid.HttpMaidChainKeys.*;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class MarshalProcessor implements Processor {
    private final Marshallers marshallers;
    private final boolean throwExceptionIfNoMarshallerFound;

    public static MarshalProcessor marshalProcessor(final Marshallers marshallers,
                                                    final boolean throwExceptionIfNoMarshallerFound) {
        return new MarshalProcessor(marshallers, throwExceptionIfNoMarshallerFound);
    }

    @Override
    public void apply(final MetaData metaData) {
        try {
            metaData.getOptional(RESPONSE_BODY_OBJECT).ifPresent(map -> {
                final ContentType responseContentType = marshallers.determineResponseContentType(metaData);
                final Marshaller marshaller = marshallers.marshallerFor(responseContentType);
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

    private void failIfConfiguredToDoSo(final Supplier<RuntimeException> exceptionSupplier) {
        if (throwExceptionIfNoMarshallerFound) {
            throw exceptionSupplier.get();
        }
    }
}
