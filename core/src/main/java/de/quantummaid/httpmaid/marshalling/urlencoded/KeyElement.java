/*
 * Copyright (c) 2019 Richard Hauswald - https://quantummaid.de/.
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

package de.quantummaid.httpmaid.marshalling.urlencoded;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.quantummaid.httpmaid.util.Validators.validateNotNull;
import static java.lang.Integer.parseInt;
import static java.lang.String.format;
import static java.net.URLDecoder.decode;
import static java.nio.charset.StandardCharsets.UTF_8;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
final class KeyElement {
    private static final Pattern ARRAY_INDEX_PATTERN = Pattern.compile("[0-9]+");

    private final String value;

    static KeyElement keyElement(final String value) {
        validateNotNull(value, "value");
        return new KeyElement(value);
    }

    boolean isArrayIndex() {
        final Matcher matcher = ARRAY_INDEX_PATTERN.matcher(this.value);
        return matcher.matches();
    }

    int asArrayIndex() {
        return parseInt(this.value);
    }

    String value() {
        return this.value;
    }

    String renderAsFirstElement() {
        return decode(this.value, UTF_8);
    }

    String renderAsIndexElement() {
        return format("[%s]", renderAsFirstElement());
    }
}
