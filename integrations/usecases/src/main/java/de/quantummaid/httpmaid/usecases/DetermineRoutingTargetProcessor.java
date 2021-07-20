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

package de.quantummaid.httpmaid.usecases;

import de.quantummaid.httpmaid.chains.MetaData;
import de.quantummaid.httpmaid.chains.Processor;
import de.quantummaid.httpmaid.generator.Generators;
import de.quantummaid.usecasemaid.RoutingTarget;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import static de.quantummaid.httpmaid.usecases.UseCasesModule.ROUTING_TARGET;
import static de.quantummaid.httpmaid.util.Validators.validateNotNull;
import static lombok.AccessLevel.PRIVATE;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = PRIVATE)
public class DetermineRoutingTargetProcessor implements Processor {
    private final Generators<RoutingTarget> generators;

    public static Processor determineRoutingTargetProcessor(final Generators<RoutingTarget> generators) {
        validateNotNull(generators, "generators");
        return new DetermineRoutingTargetProcessor(generators);
    }

    @Override
    public void apply(final MetaData metaData) {
        generators.generate(metaData)
                .ifPresent(routingTarget -> metaData.set(ROUTING_TARGET, routingTarget));
    }
}
