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

import java.util.ArrayList;
import java.util.List;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Enrichable {
    private final String key;
    private Object value;
    private final List<InternalEnricher> enrichersWithoutOverwrite = new ArrayList<>();
    private final List<InternalEnricher> enrichersWithOverwrite = new ArrayList<>();

    public static Enrichable enrichable(final String key) {
        return new Enrichable(key);
    }

    public void setValue(final Object value) {
        this.value = value;
    }

    public void enrichWithoutOverwrite(final InternalEnricher enricher) {
        enrichersWithoutOverwrite.add(enricher);
    }

    public void enrichWithOverwrite(final InternalEnricher enricher) {
        enrichersWithOverwrite.add(enricher);
    }

    public Object compile() {
        for (final InternalEnricher enricher : enrichersWithoutOverwrite) {
            value = enricher.enrich(key, value);
        }
        for (final InternalEnricher enricher : enrichersWithOverwrite) {
            value = enricher.enrich(key, value);
        }
        return value;
    }
}
