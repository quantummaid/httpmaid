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

package de.quantummaid.httpmaid.events.enriching;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.quantummaid.httpmaid.events.enriching.AddMapEntryEnricher.mapEntry;
import static de.quantummaid.httpmaid.events.enriching.Enrichable.enrichable;
import static de.quantummaid.httpmaid.util.Validators.validateNotNull;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class EnrichableMap {
    private final Map<String, Enrichable> map;

    public static EnrichableMap emptyEnrichableMap() {
        return new EnrichableMap(new HashMap<>());
    }

    public static EnrichableMap enrichableMap(final List<String> topLevelKeys) {
        validateNotNull(topLevelKeys, "topLevelKeys");
        final Map<String, Enrichable> map = new HashMap<>(topLevelKeys.size());
        topLevelKeys.forEach(key -> map.put(key, enrichable(key)));
        return new EnrichableMap(map);
    }

    public Map<String, Object> asMap() {
        final Map<String, Object> compiled = new HashMap<>();
        map.forEach((key, enrichable) -> {
            final Object value = enrichable.compile();
            compiled.put(key, value);
        });
        return compiled;
    }

    public void overwriteTopLevel(final Map<String, ?> values) {
        values.forEach(this::overwriteTopLevel);
    }

    public void overwriteTopLevel(final String key, final Object value) {
        if (!map.containsKey(key)) {
            return;
        }
        map.get(key).setValue(value);
    }

    public void enrichEitherTopOrSecondLevelWithoutOverwriting(final Map<String, ?> values) {
        values.forEach(this::enrichEitherTopOrSecondLevelWithoutOverwriting);
    }

    public void enrichEitherTopOrSecondLevelWithoutOverwriting(final String key, final Object value) {
        if (map.containsKey(key) && map.get(key) == null) {
            overwriteTopLevel(key, value);
        } else {
            enrichOnSecondLevelWithoutOverwriting(key, value);
        }
    }

    public void enrichOnSecondLevelWithoutOverwriting(final String key, final Object value) {
        this.map.values().forEach(enrichable -> enrichWithoutOverwriting(key, value, enrichable));
    }

    public void enrichOnSecondLevelWithOverwriting(final String key, final Object value) {
        this.map.values().forEach(enrichable -> enrichWithOverwriting(key, value, enrichable));
    }

    private static void enrichWithoutOverwriting(final String key,
                                                 final Object value,
                                                 final Enrichable enrichable) {
        enrichable.enrichWithoutOverwrite(mapEntry(key, value));
    }

    private static void enrichWithOverwriting(final String key,
                                              final Object value,
                                              final Enrichable enrichable) {
        enrichable.enrichWithOverwrite(mapEntry(key, value));
    }
}