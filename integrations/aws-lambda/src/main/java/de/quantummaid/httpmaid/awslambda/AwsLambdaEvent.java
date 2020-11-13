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

package de.quantummaid.httpmaid.awslambda;

import de.quantummaid.httpmaid.chains.MetaDataKey;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import static de.quantummaid.httpmaid.awslambda.LambdaEventException.*;
import static de.quantummaid.httpmaid.chains.MetaDataKey.metaDataKey;
import static java.util.Objects.requireNonNullElseGet;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class AwsLambdaEvent {
    public static final MetaDataKey<AwsLambdaEvent> AWS_LAMBDA_EVENT = metaDataKey("AWS_LAMBDA_EVENT");

    private final Map<String, Object> event;

    public static AwsLambdaEvent awsLambdaEvent(final Map<String, Object> event) {
        if (event.isEmpty()) {
            throw emptyLambdaEventException();
        }
        return new AwsLambdaEvent(event);
    }

    public Optional<String> getAsOptionalString(final String key) {
        return getAsOptional(key, String.class);
    }

    public String getAsString(final String key) {
        return getAs(key, String.class);
    }

    public Boolean getAsBoolean(final String key) {
        return getAs(key, Boolean.class);
    }

    @SuppressWarnings("unchecked")
    public <T> T getOrDefault(final String key, final Supplier<T> alternative) {
        final T value = (T) event.get(key);
        return requireNonNullElseGet(value, alternative);
    }

    @SuppressWarnings("unchecked")
    public AwsLambdaEvent getMap(final String key) {
        final Map<String, Object> map = getAs(key, Map.class);
        return new AwsLambdaEvent(map);
    }

    private <T> Optional<T> getAsOptional(final String key, final Class<T> type) {
        final Object value = event.get(key);
        if (value == null) {
            return Optional.empty();
        }
        return Optional.of(castSafely(key, value, type));
    }

    private <T> T getAs(final String key, final Class<T> type) {
        if (!event.containsKey(key)) {
            throw unknownKeyException(key, event);
        }
        final Object value = event.get(key);
        return castSafely(key, value, type);
    }

    @SuppressWarnings("unchecked")
    private <T> T castSafely(final String key,
                             final Object value,
                             final Class<T> type) {
        if (!type.isInstance(value)) {
            throw wrongTypeException(key, type, value, event);
        }
        return (T) value;
    }
}
