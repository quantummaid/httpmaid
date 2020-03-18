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

package de.quantummaid.httpmaid.handler.distribution;

import de.quantummaid.httpmaid.PerRouteConfigurator;
import de.quantummaid.httpmaid.generator.GenerationCondition;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.List;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class DistributableHandler {
    private final GenerationCondition condition;
    private final Object handler;
    private final List<PerRouteConfigurator> perRouteConfigurators;

    public static DistributableHandler distributableHandler(final GenerationCondition condition,
                                                            final Object handler,
                                                            final List<PerRouteConfigurator> perRouteConfigurators) {
        return new DistributableHandler(condition, handler, perRouteConfigurators);
    }

    public GenerationCondition condition() {
        return condition;
    }

    public Object handler() {
        return handler;
    }

    public List<PerRouteConfigurator> perRouteConfigurators() {
        return perRouteConfigurators;
    }
}
