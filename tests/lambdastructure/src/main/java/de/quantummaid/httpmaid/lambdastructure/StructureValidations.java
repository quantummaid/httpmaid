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

package de.quantummaid.httpmaid.lambdastructure;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.List;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class StructureValidations {
    private final List<Validation> validations;

    public static StructureValidations ok() {
        return new StructureValidations(List.of());
    }

    public static StructureValidations validation(final String validation) {
        final Validation error = Validation.validation(validation);
        return new StructureValidations(List.of(error));
    }

    public static StructureValidations validations(final List<StructureValidations> validations) {
        final List<Validation> list = validations.stream()
                .map(structureValidations -> structureValidations.validations)
                .flatMap(Collection::stream)
                .collect(toList());
        return new StructureValidations(list);
    }

    public boolean isValid() {
        return validations.isEmpty();
    }

    public StructureValidations rebase(final String base) {
        final List<Validation> rebased = validations.stream()
                .map(validation -> validation.base(base))
                .collect(toList());
        return new StructureValidations(rebased);
    }

    public String render() {
        return validations.stream()
                .map(Validation::render)
                .collect(joining("\n"));
    }
}
