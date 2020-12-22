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

package de.quantummaid.httpmaid.documentation.support.curl.tokens;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.List;

import static java.util.Arrays.asList;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class TokenStream {
    private final List<String> tokens;
    private int currentLocation = 0;

    public static TokenStream splitToTokenStream(final String string, final String splitRegex) {
        final String[] split = string.split(splitRegex);
        return new TokenStream(asList(split));
    }

    public static TokenStream tokenStream(final List<String> tokens) {
        return new TokenStream(tokens);
    }

    public boolean hasNext() {
        return currentLocation < tokens.size();
    }

    public String next() {
        final String token = tokens.get(currentLocation);
        currentLocation = currentLocation + 1;
        return token;
    }
}
