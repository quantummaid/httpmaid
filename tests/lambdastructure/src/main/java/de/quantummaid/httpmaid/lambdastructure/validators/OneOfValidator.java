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

import static de.quantummaid.httpmaid.lambdastructure.StructureValidations.ok;
import static de.quantummaid.httpmaid.lambdastructure.StructureValidations.validations;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class OneOfValidator implements StructureValidator {
    private final List<StructureValidator> options;

    public static StructureValidator oneOf(final StructureValidator... validators) {
        return new OneOfValidator(asList(validators));
    }

    @Override
    public StructureValidations validate(final Object object) {
        final List<StructureValidations> invalid = options.stream()
                .map(validator -> validator.validate(object))
                .filter(validations -> !validations.isValid())
                .collect(toList());

        if (invalid.size() != options.size()) {
            return ok();
        } else {
            final List<StructureValidations> rebasedValidations = new ArrayList<>(invalid.size());
            for (int i = 0; i < invalid.size(); ++i) {
                final StructureValidations rebased = invalid.get(i).rebase("alternative" + i);
                rebasedValidations.add(rebased);
            }
            return validations(rebasedValidations);
        }
    }

    @Override
    public Object mutableSample() {
        final StructureValidator validator = options.get(0);
        return validator.mutableSample();
    }
}
