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
import de.quantummaid.httpmaid.http.headers.ContentType;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;

import static de.quantummaid.httpmaid.HttpMaidChains.*;
import static de.quantummaid.httpmaid.marshalling.Marshallers.marshallers;
import static de.quantummaid.httpmaid.marshalling.Unmarshallers.unmarshallers;
import static de.quantummaid.httpmaid.marshalling.processors.MarshalProcessor.marshalProcessor;
import static de.quantummaid.httpmaid.marshalling.processors.RegisterMarshallersProcessor.registerDefaultMarshallerProcessor;
import static de.quantummaid.httpmaid.marshalling.processors.UnmarshalProcessor.unmarshalProcessor;
import static de.quantummaid.httpmaid.util.Validators.validateNotNull;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class MarshallingModule implements ChainModule {
    private DefaultContentTypeProvider defaultContentTypeProvider;
    private final Map<ContentType, Unmarshaller> unmarshallersMap;
    private final Map<ContentType, Marshaller> marshallersMap;
    private boolean throwExceptionIfNoMarshallerFound;

    public static MarshallingModule emptyMarshallingModule() {
        return new MarshallingModule(new HashMap<>(), new HashMap<>());
    }

    public void addUnmarshaller(final ContentType contentType, final Unmarshaller unmarshaller) {
        validateNotNull(contentType, "contentType");
        validateNotNull(unmarshaller, "unmarshaller");
        unmarshallersMap.put(contentType, unmarshaller);
        if (defaultContentTypeProvider == null) {
            setDefaultContentTypeProvider(contentType);
        }
    }

    public void addMarshaller(final ContentType contentType, final Marshaller marshaller) {
        validateNotNull(contentType, "contentType");
        validateNotNull(marshaller, "marshaller");
        marshallersMap.put(contentType, marshaller);
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
        final Unmarshallers unmarshallers = unmarshallers(unmarshallersMap);
        extender.appendProcessor(PROCESS_BODY_STRING, unmarshalProcessor(
                unmarshallers,
                throwExceptionIfNoMarshallerFound,
                defaultContentTypeProvider
        ));
        final Marshallers marshallers = marshallers(marshallersMap, defaultContentTypeProvider);
        extender.prependProcessor(POST_INVOKE, marshalProcessor(
                marshallers,
                throwExceptionIfNoMarshallerFound
        ));
        extender.appendProcessor(INIT, registerDefaultMarshallerProcessor(marshallers));
    }
}
