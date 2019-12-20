/*
 * Copyright (c) 2019 Richard Hauswald - https://quantummaid.de/.
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

package de.quantummaid.httpmaid.security;

import de.quantummaid.httpmaid.handler.http.HttpRequest;
import de.quantummaid.httpmaid.path.Path;
import de.quantummaid.httpmaid.path.PathTemplate;

import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;

public interface Filter {

    static Filter pathsFilter(final String... paths) {
        final List<PathTemplate> pathTemplates = Arrays.stream(paths)
                .map(PathTemplate::pathTemplate)
                .collect(toList());
        return request -> {
            final Path path = request.path();
            return pathTemplates.stream()
                    .anyMatch(pathTemplate -> pathTemplate.matches(path));
        };
    }

    boolean filter(HttpRequest request);
}
