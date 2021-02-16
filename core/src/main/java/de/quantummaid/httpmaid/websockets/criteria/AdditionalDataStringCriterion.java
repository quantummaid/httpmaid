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

package de.quantummaid.httpmaid.websockets.criteria;

import de.quantummaid.httpmaid.mappath.MapPath;
import de.quantummaid.httpmaid.mappath.Retrieval;
import de.quantummaid.httpmaid.websockets.registry.WebsocketRegistryEntry;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Map;

import static de.quantummaid.reflectmaid.validators.NotNullValidator.validateNotNull;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class AdditionalDataStringCriterion {
    private final MapPath key;
    private final String value;

    public static AdditionalDataStringCriterion additionalDataStringCriterion(final MapPath key,
                                                                              final String value) {
        validateNotNull(key, "key");
        validateNotNull(value, "value");
        return new AdditionalDataStringCriterion(key, value);
    }

    public boolean filter(final WebsocketRegistryEntry entry) {
        final Map<String, Object> additionalData = entry.additionalData();
        final Retrieval retrieval = key.retrieveOptionally(additionalData);
        if (retrieval.isError()) {
            return false;
        } else {
            final Object retrievedValue = retrieval.value();
            return value.equals(retrievedValue);
        }
    }

    public MapPath key() {
        return key;
    }

    public String value() {
        return value;
    }
}
