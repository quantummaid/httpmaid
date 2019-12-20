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

package de.quantummaid.httpmaid.http.headers;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class HeaderValueWithComment {
    private static final Pattern PATTERN = Pattern.compile("(?<value>[^;]*)(;(?<comment>.*))?");

    private final String value;
    @EqualsAndHashCode.Exclude
    private final String comment;

    public static HeaderValueWithComment fromString(final String rawValue) {
        final Matcher matcher = PATTERN.matcher(rawValue);
        if (!matcher.matches()) {
            throw new IllegalArgumentException(format("Header with comment '%s' must match '%s'", rawValue, PATTERN.pattern()));
        }
        final String value = matcher.group("value").toLowerCase();
        final String comment = ofNullable(matcher.group("comment")).orElse("");
        return new HeaderValueWithComment(value, comment);
    }

    public String value() {
        return value;
    }

    public String comment() {
        return comment;
    }

    public String valueWithComment() {
        if(comment == null || comment.isEmpty()) {
            return value();
        }
        return String.format("%s;%s", value, comment);
    }
}
