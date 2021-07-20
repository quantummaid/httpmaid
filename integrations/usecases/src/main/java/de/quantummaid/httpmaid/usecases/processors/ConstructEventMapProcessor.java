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

package de.quantummaid.httpmaid.usecases.processors;

import de.quantummaid.httpmaid.chains.MetaData;
import de.quantummaid.httpmaid.chains.Processor;
import de.quantummaid.httpmaid.usecases.eventfactories.EventFactory;
import de.quantummaid.httpmaid.usecases.eventfactories.enriching.EnrichableMap;
import de.quantummaid.httpmaid.usecases.eventfactories.enriching.Event;
import de.quantummaid.usecasemaid.RoutingTarget;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.Map;

import static de.quantummaid.httpmaid.HttpMaidChainKeys.UNMARSHALLED_REQUEST_BODY;
import static de.quantummaid.httpmaid.usecases.UseCasesModule.EVENT;
import static de.quantummaid.httpmaid.usecases.UseCasesModule.ROUTING_TARGET;
import static de.quantummaid.httpmaid.usecases.eventfactories.enriching.Event.event;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ConstructEventMapProcessor implements Processor {
    private final Map<RoutingTarget, EventFactory> eventFactories;

    public static Processor constructEventMapProcessor(final Map<RoutingTarget, EventFactory> eventFactories) {
        return new ConstructEventMapProcessor(eventFactories);
    }

    @Override
    public void apply(final MetaData metaData) {
        final Object unmarshalled = metaData.getOptional(UNMARSHALLED_REQUEST_BODY).orElse(null);
        final RoutingTarget routingTarget = metaData.get(ROUTING_TARGET);
        final EventFactory eventFactory = eventFactories.get(routingTarget);
        final EnrichableMap map = eventFactory.createEvent(unmarshalled);
        final Event event = event(map);
        metaData.set(EVENT, event);
    }
}
