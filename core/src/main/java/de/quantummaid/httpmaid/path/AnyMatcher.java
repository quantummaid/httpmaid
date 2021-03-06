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

package de.quantummaid.httpmaid.path;

import de.quantummaid.httpmaid.path.statemachine.StateMachineMatcher;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Collections.emptyList;

@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class AnyMatcher implements StateMachineMatcher<String> {

    static boolean isRecursiveWildcard(final String stringSpecification) {
        return "*".equals(stringSpecification);
    }

    static StateMachineMatcher<String> anyMatcher() {
        return new AnyMatcher();
    }

    @Override
    public Optional<Map<String, String>> matchAndReturnCaptures(final String element) {
        return Optional.of(Map.of());
    }

    @Override
    public List<String> captures() {
        return emptyList();
    }

    @Override
    public String toString() {
        return "*";
    }
}
