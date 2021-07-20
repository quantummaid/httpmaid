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

package de.quantummaid.httpmaid.usecases.mapmaid;

import de.quantummaid.httpmaid.exceptions.HttpExceptionMapper;
import de.quantummaid.httpmaid.handler.http.HttpRequest;
import de.quantummaid.httpmaid.handler.http.HttpResponse;
import de.quantummaid.mapmaid.mapper.deserialization.validation.AggregatedValidationException;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class MapMaidValidationExceptionMapper implements HttpExceptionMapper<Throwable> {
    private final int statusCode;

    public static HttpExceptionMapper<Throwable> mapMaidValidationExceptionMapper(final int statusCode) {
        return new MapMaidValidationExceptionMapper(statusCode);
    }

    @Override
    public void map(final Throwable exception,
                    final HttpRequest request,
                    final HttpResponse response) {
        if (!(exception instanceof AggregatedValidationException)) {
            throw new UnsupportedOperationException(
                    format("%s can only be registered for %s but got %s. This should never happen.",
                            MapMaidValidationExceptionMapper.class.getSimpleName(),
                            AggregatedValidationException.class.getSimpleName(),
                            exception)
            );
        }
        final AggregatedValidationException aggregatedException = (AggregatedValidationException) exception;
        final List<Object> errorsList = aggregatedException.getValidationErrors()
                .stream()
                .map(validationError -> {
                    final Map<String, String> map = new HashMap<>();
                    map.put("path", validationError.propertyPath);
                    if (validationError.message != null) {
                        map.put("message", validationError.message);
                    }
                    return map;
                })
                .collect(toList());
        response.setBody(Map.of("errors", errorsList));
        response.setStatus(statusCode);
    }
}
