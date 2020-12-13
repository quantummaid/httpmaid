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

package de.quantummaid.httpmaid.events.enriching.enrichers;

import de.quantummaid.httpmaid.handler.http.HttpRequest;
import de.quantummaid.httpmaid.mappath.MapPath;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Map;
import java.util.Optional;

import static java.util.Optional.of;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class AdditionalDataEnricher implements TopEnricher {
    private final MapPath additionalDataPath;
    private final String mapKey;

    public static AdditionalDataEnricher additionalDataEnricher(final MapPath additionalDataPath,
                                                                final String mapKey) {
        return new AdditionalDataEnricher(additionalDataPath, mapKey);
    }

    @Override
    public String mapKey() {
        return mapKey;
    }

    @Override
    public Optional<String> extractValue(final HttpRequest request) {
        final Map<String, Object> additionalData = request.additionalData();
        final String retrieved = (String) additionalDataPath.retrieve(additionalData);
        return of(retrieved);
    }
}
