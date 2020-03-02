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

package de.quantummaid.httpmaid.generator;

import de.quantummaid.httpmaid.chains.MetaData;
import de.quantummaid.httpmaid.util.Validators;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.List;
import java.util.Optional;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Generators<T> {
    private final List<Generator<T>> generators;

    public static <T> Generators<T> generators(final List<Generator<T>> generators) {
        Validators.validateNotNull(generators, "generators");
        validateForConflicts(generators);
        return new Generators<>(generators);
    }

    private static <T> void validateForConflicts(final List<Generator<T>> generators) {
        for (int i = 0; i < generators.size(); ++i) {
            final Generator<T> generatorA = generators.get(i);
            for (int j = i + 1; j < generators.size(); ++j) {
                final Generator<T> generatorB = generators.get(j);
                if (generatorA.isSubsetOf(generatorB)) {
                    throw OverlappingConditionsException.overlappingConditionsException(generatorA, generatorB);
                }
            }
        }
    }

    public Optional<T> generate(final MetaData metaData) {
        return generators.stream()
                .map(generator -> generator.generate(metaData))
                .filter(Optional::isPresent)
                .map(Optional::orElseThrow)
                .findFirst();
    }
}
