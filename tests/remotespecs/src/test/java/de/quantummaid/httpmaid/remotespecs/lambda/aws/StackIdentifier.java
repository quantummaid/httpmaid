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

package de.quantummaid.httpmaid.remotespecs.lambda.aws;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Optional.ofNullable;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class StackIdentifier {
    private static final Pattern PATTERN = Pattern.compile("[a-zA-Z0-9]+");

    private static final String SHARED_STACK_PREFIX = "remotespecs";
    private static final String REMOTESPECS_STACK_IDENTIFIER_ENV = "REMOTESPECS_STACK_IDENTIFIER";

    private final String value;

    public static Optional<StackIdentifier> userProvidedStackIdentifier() {
        final String stackIdentifier = System.getenv(REMOTESPECS_STACK_IDENTIFIER_ENV);
        return ofNullable(stackIdentifier).map(s -> "dev" + s)
                .map(StackIdentifier::stackIdentifier);
    }

    public static StackIdentifier sharedStackIdentifier() {
        final String uuid = UUID.randomUUID().toString().substring(0, 7);
        final String value = String.format("%s%s", SHARED_STACK_PREFIX, uuid);
        return stackIdentifier(value);
    }

    public static StackIdentifier stackIdentifier(final String value) {
        final Matcher matcher = PATTERN.matcher(value);
        if (!matcher.matches()) {
            throw new IllegalStateException(
                    "stack identifier must match " + PATTERN.pattern() + " but was " + value);
        }
        return new StackIdentifier(value);
    }

    public String value() {
        return value;
    }
}
