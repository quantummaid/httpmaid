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

import java.util.ArrayList;
import java.util.List;

import static java.lang.String.join;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Validation {
    private final List<String> path;
    private final String error;

    public static Validation validation(final String error) {
        return new Validation(new ArrayList<>(), error);
    }

    public Validation base(final String base) {
        return base(List.of(base));
    }

    public Validation base(final List<String> base) {
        final List<String> newPath = new ArrayList<>(base);
        newPath.addAll(path);
        return new Validation(newPath, error);
    }

    public String render() {
        final String joinedPath = join(".", path);
        return joinedPath + ": " + error;
    }
}
