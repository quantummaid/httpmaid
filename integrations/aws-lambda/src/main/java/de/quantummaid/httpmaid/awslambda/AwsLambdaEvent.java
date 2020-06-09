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

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Map;
import java.util.function.Supplier;

import static de.quantummaid.httpmaid.awslambda.AwsLambdaEventKeys.HTTP_METHOD;
import static de.quantummaid.httpmaid.awslambda.AwsLambdaEventKeys.REQUEST_CONTEXT;
import static de.quantummaid.httpmaid.awslambda.EmptyLambdaEventException.emptyLambdaEventException;
import static java.util.Objects.requireNonNullElse;
import static java.util.Objects.requireNonNullElseGet;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class AwsLambdaEvent {
    private final Map<String, Object> event;
    private final Map<String, Object> requestContext;

    @SuppressWarnings("unchecked")
    public static AwsLambdaEvent awsLambdaEvent(final Map<String, Object> event) {
        if (event.isEmpty()) {
            throw emptyLambdaEventException();
        }
        final Map<String, Object> requestContext = (Map<String, Object>) event.get(REQUEST_CONTEXT);
        return new AwsLambdaEvent(event, requestContext);
    }

    public String getAsString(final String key) {
        return (String) event.get(key);
    }

    @SuppressWarnings("unchecked")
    public <T> T getOrDefault(final String key, final T alternative) {
        final T value = (T) event.get(key);
        return requireNonNullElse(value, alternative);
    }

    @SuppressWarnings("unchecked")
    public <T> T getOrDefault(final String key, final Supplier<T> alternative) {
        final T value = (T) event.get(key);
        return requireNonNullElseGet(value, alternative);
    }

    public String getFromContext(final String key) {
        return (String) requestContext.get(key);
    }

    public boolean isWebSocketRequest() {
        return isWebSocketRequest(event);
    }

    public static boolean isWebSocketRequest(final Map<String, Object> event) {
        return !event.containsKey(HTTP_METHOD);
    }
}
