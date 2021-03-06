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

package de.quantummaid.httpmaid.chains;

import de.quantummaid.httpmaid.util.Validators;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import static de.quantummaid.httpmaid.chains.ChainException.chainException;
import static java.lang.String.format;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;

@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class MetaData {
    private final Map<String, Object> map;

    public static MetaData emptyMetaData() {
        return new MetaData(new HashMap<>());
    }

    public <T> void set(final MetaDataKey<T> key, final T value) {
        Validators.validateNotNull(key, "key");
        map.put(key.key(), value);
    }

    public void setUnchecked(final MetaDataKey<?> key, final Object value) {
        Validators.validateNotNull(key, "key");
        map.put(key.key(), value);
    }

    public <T> T get(final MetaDataKey<T> key) {
        return getOptional(key).orElseThrow(() -> new RuntimeException(format(
                "Could not find meta datum %s in %s", key.key(), map)));
    }

    public <T> T getOrSetDefault(final MetaDataKey<T> key, final Supplier<T> defaultProvider) {
        final Optional<T> optional = getOptional(key);
        if (optional.isPresent()) {
            return optional.get();
        }
        final T defaultValue = defaultProvider.get();
        set(key, defaultValue);
        return defaultValue;
    }

    @SuppressWarnings("unchecked")
    public <T> T getAs(final MetaDataKey<? super T> key, final Class<T> type) {
        final Object object = get(key);
        ensureIsOfType(object, type, key);
        return (T) object;
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<T> getOptional(final MetaDataKey<T> key) {
        final T datum = (T) map.get(key.key());
        return ofNullable(datum);
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<T> getOptionalAs(final MetaDataKey<? super T> key, final Class<T> type) {
        final Optional<?> optional = getOptional(key);
        optional.ifPresent(o -> ensureIsOfType(o, type, key));
        return (Optional<T>) optional;
    }

    public boolean contains(final MetaDataKey<?> key) {
        return getOptional(key).isPresent();
    }

    @Override
    public String toString() {
        return prettyPrint();
    }

    public String prettyPrint() {
        return map.entrySet().stream()
                .map(entry -> format("%s = %s", entry.getKey(), entry.getValue()))
                .collect(joining("\n"));
    }

    private static void ensureIsOfType(final Object object,
                                       final Class<?> type,
                                       final MetaDataKey<?> key) {
        if (!type.isInstance(object)) {
            throw chainException(format(
                    "Object '%s' (%s) needs to be of type '%s'",
                    object, key.key(), type.getName()));
        }
    }
}
