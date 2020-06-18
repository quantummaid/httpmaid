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

package de.quantummaid.httpmaid.http.headers.accept;

import de.quantummaid.httpmaid.util.Validators;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.List;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class MimeTypeMatcher {
    private final MimeTypeElementMatcher type;
    private final MimeTypeElementMatcher subtype;

    static MimeTypeMatcher anyMatcher() {
        return parseMimeTypeMatcher(List.of("*/*"));
    }

    static MimeTypeMatcher parseMimeTypeMatcher(final List<String> mimeTypes) {
        final String combinedMimeTypes = String.join(", ", mimeTypes);
        final MimeType mimeType = MimeType.parseMimeType(combinedMimeTypes);

        final MimeTypeElementMatcher type = MimeTypeElementMatcher.parse(mimeType.type());
        final MimeTypeElementMatcher subtype = MimeTypeElementMatcher.parse(mimeType.subtype());

        return new MimeTypeMatcher(type, subtype);
    }

    public boolean matches(final MimeType mimeType) {
        Validators.validateNotNull(mimeType, "mimeType");
        return type.matches(mimeType.type()) &&
                subtype.matches(mimeType.subtype());
    }
}
