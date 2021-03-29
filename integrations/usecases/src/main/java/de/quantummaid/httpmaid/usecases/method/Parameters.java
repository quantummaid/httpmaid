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

package de.quantummaid.httpmaid.usecases.method;

import de.quantummaid.reflectmaid.resolvedtype.ResolvedType;
import de.quantummaid.reflectmaid.resolvedtype.resolver.ResolvedMethod;
import de.quantummaid.reflectmaid.resolvedtype.resolver.ResolvedParameter;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Parameters {
    private final List<ResolvedParameter> parameters;

    public static Parameters parametersOf(final ResolvedMethod method) {
        final List<ResolvedParameter> parameters = method.getParameters();
        return new Parameters(parameters);
    }

    public List<String> names() {
        return parameters.stream()
                .map(ResolvedParameter::name)
                .collect(toList());
    }

    public Map<String, ResolvedType> asMap() {
        return this.parameters.stream()
                .collect(toMap(
                        ResolvedParameter::name,
                        ResolvedParameter::getType
                ));
    }

    public Object[] toArray(final Map<String, Object> parameterInstances) {
        return this.parameters.stream()
                .map(ResolvedParameter::name)
                .map(parameterInstances::get)
                .toArray();
    }
}
