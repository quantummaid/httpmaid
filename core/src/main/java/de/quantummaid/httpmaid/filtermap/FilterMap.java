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

package de.quantummaid.httpmaid.filtermap;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static de.quantummaid.httpmaid.util.Validators.validateNotNull;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class FilterMap<F, T> {
    private final List<FilterMapEntry<F, T>> entries;
    private final T defaultValue;

    static <F, T> FilterMap<F, T> filterMap(final List<FilterMapEntry<F, T>> entries,
                                            final T defaultValue) {
        validateNotNull(entries, "entries");
        validateNotNull(defaultValue, "defaultValue");
        return new FilterMap<>(entries, defaultValue);
    }

    public T get(final F condition) {
        return entries.stream()
                .filter(entry -> entry.test(condition))
                .map(FilterMapEntry::value)
                .findFirst()
                .orElse(defaultValue);
    }
}
