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
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Unmarshallers {
    private final Map<ContentType, Unmarshaller> unmarshallers;

    public static Unmarshallers unmarshallers(final Map<ContentType, Unmarshaller> unmarshallers) {
        return new Unmarshallers(unmarshallers);
    }

    public Unmarshaller byContentType(final ContentType contentType) {
        return unmarshallers.get(contentType);
    }

    public boolean supportsContentType(final ContentType contentType) {
        return unmarshallers.containsKey(contentType);
    }

    public List<ContentType> supportedContentTypes() {
        return new ArrayList<>(unmarshallers.keySet());
    }
}
