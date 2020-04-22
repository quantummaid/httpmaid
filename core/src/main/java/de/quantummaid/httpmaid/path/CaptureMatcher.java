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
import de.quantummaid.httpmaid.util.Validators;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.quantummaid.httpmaid.path.PathException.pathException;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
final class CaptureMatcher implements StateMachineMatcher<String> {

    private static final Pattern PATTERN = Pattern.compile("<(.*)>");
    private final String name;

    static boolean isWildcard(final String stringSpecification) {
        final Matcher matcher = PATTERN.matcher(stringSpecification);
        return matcher.matches();
    }

    static StateMachineMatcher<String> fromStringSpecification(final String stringSpecification) {
        Validators.validateNotNullNorEmpty(stringSpecification, "stringSpecification");
        final Matcher matcher = PATTERN.matcher(stringSpecification);
        if(!matcher.matches()) {
            throw pathException("Not a wildcard: " + stringSpecification);
        }
        final String name = matcher.group(1);
        return new CaptureMatcher(name);
    }

    @Override
    public Optional<Map<String, String>> matchAndReturnCaptures(final String element) {
        return Optional.of(Map.of(name, element));
    }

    @Override
    public List<String> captures() {
        return List.of(name);
    }
}
