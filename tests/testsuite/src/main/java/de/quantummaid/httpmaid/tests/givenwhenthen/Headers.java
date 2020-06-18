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

package de.quantummaid.httpmaid.tests.givenwhenthen;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import static de.quantummaid.httpmaid.tests.givenwhenthen.Header.header;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Headers {
    private final List<Header> headers;

    public static Headers emptyHeaders() {
        return new Headers(new ArrayList<>());
    }

    public boolean containsName(final String key) {
        return headers.stream()
                .map(Header::getName)
                .map(String::toLowerCase)
                .anyMatch(key::equals);
    }

    public void add(final String name,
                    final String key) {
        headers.add(header(name, key));
    }

    public void forEach(final BiConsumer<String, String> consumer) {
        headers.forEach(header -> {
                    final String name = header.getName();
                    final String value = header.getValue();
                    consumer.accept(name, value);
                });
    }
}
