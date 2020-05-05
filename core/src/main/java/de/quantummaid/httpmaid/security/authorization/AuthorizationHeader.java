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

package de.quantummaid.httpmaid.security.authorization;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.quantummaid.httpmaid.util.Validators.validateNotNullNorEmpty;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.regex.Pattern.compile;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class AuthorizationHeader {
    private static final Pattern PATTERN = compile("(?<type>\\S*+) (?<credentials>\\S*+)");

    private final String type;
    private final String credentials;

    public static Optional<AuthorizationHeader> parse(final String header) {
        validateNotNullNorEmpty(header, "header");
        final Matcher matcher = PATTERN.matcher(header);
        if(!matcher.matches()) {
            return empty();
        }
        final String type = matcher.group("type");
        final String credentials = matcher.group("credentials");
        return of(new AuthorizationHeader(type, credentials));
    }

    public String type() {
        return type;
    }

    public String credentials() {
        return credentials;
    }
}
