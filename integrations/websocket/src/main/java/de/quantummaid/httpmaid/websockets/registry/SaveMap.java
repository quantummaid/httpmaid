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

package de.quantummaid.httpmaid.websockets.registry;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.*;
import java.util.function.Supplier;

import static de.quantummaid.httpmaid.util.Validators.validateNotNull;
import static java.util.Optional.ofNullable;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
final class SaveMap<K, V> {
    private final Map<K, V> map;

    static <K, V> SaveMap<K, V> saveMap() {
        return new SaveMap<>(new HashMap<>());
    }

    void put(final K key, final V value) {
        validateNotNull(key, "key");
        validateNotNull(value, "value");
        map.put(key, value);
    }

    Optional<V> get(final K key) {
        validateNotNull(key, "key");
        return ofNullable(map.get(key));
    }

    Optional<V> getAndRemove(final K key) {
        validateNotNull(key, "key");
        final Optional<V> value = ofNullable(map.get(key));
        value.ifPresent(x -> map.remove(key));
        return value;
    }

    void remove(final K key, final Supplier<RuntimeException> exceptionSupplier) {
        if(map.containsKey(key)) {
            map.remove(key);
        } else {
            throw exceptionSupplier.get();
        }
    }

    Set<V> copyOfValues() {
        return new HashSet<>(map.values());
    }

    int size() {
        return map.size();
    }
}
