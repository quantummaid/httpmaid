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

package de.quantummaid.httpmaid.lambdastructure.validators;

import de.quantummaid.httpmaid.lambdastructure.StructureValidations;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static de.quantummaid.httpmaid.lambdastructure.StructureValidations.validation;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class MapValidator implements StructureValidator {
    private final Map<String, StructureValidator> keys;
    private final Map<String, StructureValidator> optionalKeys;

    public static MapValidator map() {
        return new MapValidator(new LinkedHashMap<>(), new LinkedHashMap<>());
    }

    public MapValidator key(final String key, final StructureValidator validator) {
        keys.put(key, validator);
        return this;
    }

    public MapValidator optionalKey(final String key, final StructureValidator validator) {
        optionalKeys.put(key, validator);
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public StructureValidations validate(final Object object) {
        if (!(object instanceof Map)) {
            return validation("not a map");
        }
        final Map<String, Object> map = (Map<String, Object>) object;
        final List<StructureValidations> validations = new ArrayList<>();
        keys.forEach((key, validator) -> {
            if (!map.containsKey(key)) {
                validations.add(validation("missing key: " + key));
                return;
            }
            final Object value = map.get(key);
            final StructureValidations valueValidations = validator.validate(value);
            final StructureValidations rebased = valueValidations.rebase(key);
            validations.add(rebased);
        });
        optionalKeys.forEach((key, validator) -> {
            if (!map.containsKey(key)) {
                return;
            }
            final Object value = map.get(key);
            final StructureValidations valueValidations = validator.validate(value);
            final StructureValidations rebased = valueValidations.rebase(key);
            validations.add(rebased);
        });
        map.keySet().stream()
                .filter(key -> !keys.containsKey(key))
                .filter(key -> !optionalKeys.containsKey(key))
                .forEach(key -> validations.add(validation("unknown key: " + key)));
        return StructureValidations.validations(validations);
    }

    @Override
    public Object mutableSample() {
        final Map<String, Object> sample = new LinkedHashMap<>();
        keys.forEach((key, structureValidator) -> sample.put(key, structureValidator.mutableSample()));
        return sample;
    }
}
