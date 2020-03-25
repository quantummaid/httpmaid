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

package de.quantummaid.httpmaid.events;

import de.quantummaid.httpmaid.events.enriching.EnrichableMap;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static de.quantummaid.httpmaid.util.Validators.validateNotNull;
import static java.util.Collections.unmodifiableList;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Event {
    private final EnrichableMap map;
    private final List<Object> typeInjections;

    public static Event event(final EnrichableMap map) {
        return new Event(map, new ArrayList<>());
    }

    public void enrich(final String key, final Object value) {
        map.enrichEitherTopOrSecondLevel(key, value);
    }

    public void addTypeInjection(final Object injection) {
        validateNotNull(injection, "injection");
        this.typeInjections.add(injection);
    }

    public Map<String, Object> asMap() {
        return map.asMap();
    }

    public List<Object> typeInjections() {
        return unmodifiableList(typeInjections);
    }
}
