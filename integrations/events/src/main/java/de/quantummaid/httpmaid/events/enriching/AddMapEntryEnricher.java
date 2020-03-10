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

import java.util.Map;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class AddMapEntryEnricher implements Enricher {
    private final String key;
    private final Object value;

    public static Enricher mapEntry(final String key,
                                    final Object value) {
        return new AddMapEntryEnricher(key, value);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object enrich(final String key, final Object enrichable) {
        if (this.key.equals(key)) {
            return value;
        }
        if (enrichable instanceof Map) {
            final Map<String, Object> map = (Map<String, Object>) enrichable;
            if (!map.containsKey(this.key)) {
                map.put(this.key, value);
            }
        }
        return enrichable;
    }

    @Override
    public String description() {
        return String.format("<%s, %s>", key, value);
    }
}
