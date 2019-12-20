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

package de.quantummaid.httpmaid.generator;

import de.quantummaid.httpmaid.chains.MetaData;
import de.quantummaid.httpmaid.http.HttpRequestMethod;
import de.quantummaid.httpmaid.path.Path;
import de.quantummaid.httpmaid.path.PathTemplate;
import de.quantummaid.httpmaid.util.Validators;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.List;
import java.util.Map;

import static de.quantummaid.httpmaid.HttpMaidChainKeys.*;
import static de.quantummaid.httpmaid.http.HttpRequestMethod.*;
import static de.quantummaid.httpmaid.http.PathParameters.pathParameters;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class PathAndMethodGenerationCondition implements GenerationCondition {
    private static final List<HttpRequestMethod> METHOD_WHITELIST = asList(GET, POST, PUT, DELETE);

    private final PathTemplate pathTemplate;
    private final HttpRequestMethod[] methods;

    public static PathAndMethodGenerationCondition pathAndMethodEventTypeGenerationCondition(
            final PathTemplate pathTemplate,
            final HttpRequestMethod... methods) {
        Validators.validateNotNull(pathTemplate, "pathTemplate");
        Validators.validateArrayNeitherNullNorEmptyNorContainsNull(methods, "methods");
        stream(methods).forEach(method -> {
            if (!METHOD_WHITELIST.contains(method)) {
                throw new IllegalArgumentException(format("Use cases cannot be registered on the %s method.", method.name()));
            }
        });
        return new PathAndMethodGenerationCondition(pathTemplate, methods);
    }

    @Override
    public boolean isSubsetOf(final GenerationCondition other) {
        Validators.validateNotNull(other, "other");
        return equals(other);
    }

    @Override
    public boolean generate(final MetaData metaData) {
        if(!metaData.contains(METHOD)) {
            return false;
        }
        final HttpRequestMethod method = metaData.get(METHOD);
        if (stream(methods).noneMatch(method::equals)) {
            return false;
        }
        if(!metaData.contains(PATH)) {
            return false;
        }
        final Path path = metaData.get(PATH);
        if (!pathTemplate.matches(path)) {
            return false;
        }

        final Map<String, String> pathParameters = pathTemplate.extractPathParameters(path);
        metaData.set(PATH_PARAMETERS, pathParameters(pathParameters));
        return true;
    }
}
