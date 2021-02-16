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
public final class StringMapValidator implements StructureValidator {
    private final StructureValidator valueValidator;

    public static StructureValidator mapOf(final StructureValidator valueValidator) {
        return new StringMapValidator(valueValidator);
    }

    @SuppressWarnings("unchecked")
    @Override
    public StructureValidations validate(final Object object) {
        if (!(object instanceof Map)) {
            return validation("not a map");
        }
        final Map<String, ?> map = (Map<String, ?>) object;
        final List<StructureValidations> validations = new ArrayList<>();
        map.forEach((key, value) -> {
            final StructureValidations valueValidations = valueValidator.validate(value).rebase(key);
            validations.add(valueValidations);
        });
        return StructureValidations.validations(validations);
    }

    @Override
    public Object mutableSample() {
        return new LinkedHashMap<>();
    }
}
