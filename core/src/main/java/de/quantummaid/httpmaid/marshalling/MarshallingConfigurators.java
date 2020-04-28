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

import de.quantummaid.httpmaid.http.headers.ContentType;

import static de.quantummaid.httpmaid.util.Validators.validateNotNull;

public final class MarshallingConfigurators {

    private MarshallingConfigurators() {
    }

    public static MarshallingModuleConfigurator toMarshallContentType(final ContentType contentType,
                                                                      final Unmarshaller unmarshaller,
                                                                      final Marshaller marshaller) {
        validateNotNull(contentType, "contentType"); // NOSONAR
        validateNotNull(unmarshaller, "unmarshaller");
        validateNotNull(marshaller, "marshaller");
        return marshallingModule -> {
            marshallingModule.addUnmarshaller(contentType, unmarshaller);
            marshallingModule.addMarshaller(contentType, marshaller);
        };
    }

    public static MarshallingModuleConfigurator toUnmarshallContentTypeInRequests(final ContentType contentType,
                                                                                  final Unmarshaller unmarshaller) {
        validateNotNull(contentType, "contentType"); // NOSONAR
        validateNotNull(unmarshaller, "unmarshaller");
        return marshallingModule -> marshallingModule.addUnmarshaller(contentType, unmarshaller);
    }

    public static MarshallingModuleConfigurator toMarshallContentTypeInResponses(final ContentType contentType,
                                                                                 final Marshaller marshaller) {
        validateNotNull(contentType, "contentType"); // NOSONAR
        validateNotNull(marshaller, "marshaller");
        return marshallingModule -> marshallingModule.addMarshaller(contentType, marshaller);
    }

    public static MarshallingModuleConfigurator toMarshallByDefaultUsingTheContentType(final ContentType contentType) {
        validateNotNull(contentType, "contentType"); // NOSONAR
        return marshallingModule -> marshallingModule.setDefaultContentTypeProvider(contentType);
    }

    public static MarshallingModuleConfigurator toThrowAnExceptionIfNoMarshallerWasFound() {
        return marshallingModule -> marshallingModule.setThrowExceptionIfNoMarshallerFound(true);
    }
}
