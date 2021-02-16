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
import java.util.List;

import static de.quantummaid.httpmaid.lambdastructure.StructureValidations.validation;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ListValidator implements StructureValidator {
    private final StructureValidator valueValidator;

    public static StructureValidator listOf(final StructureValidator valueValidator) {
        return new ListValidator(valueValidator);
    }

    @Override
    public StructureValidations validate(final Object object) {
        if (!(object instanceof List)) {
            return validation("not a list");
        }
        final List<?> list = (List<?>) object;
        final List<StructureValidations> validations = new ArrayList<>(list.size());
        for (int i = 0; i < list.size(); ++i) {
            final StructureValidations elementValidations = valueValidator.validate(list.get(i));
            validations.add(elementValidations.rebase("[" + i + "]"));
        }
        return StructureValidations.validations(validations);
    }

    @Override
    public Object mutableSample() {
        return new ArrayList<>();
    }
}
