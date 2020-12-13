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

package de.quantummaid.httpmaid.events.processors;

import de.quantummaid.eventmaid.processingcontext.EventType;
import de.quantummaid.httpmaid.chains.MetaData;
import de.quantummaid.httpmaid.chains.Processor;
import de.quantummaid.httpmaid.events.Event;
import de.quantummaid.httpmaid.events.EventFactory;
import de.quantummaid.httpmaid.events.enriching.EnrichableMap;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.Map;

import static de.quantummaid.httpmaid.HttpMaidChainKeys.UNMARSHALLED_REQUEST_BODY;
import static de.quantummaid.httpmaid.events.Event.event;
import static de.quantummaid.httpmaid.events.EventModule.EVENT;
import static de.quantummaid.httpmaid.events.EventModule.EVENT_TYPE;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ConstructEventMapProcessor implements Processor {
    private final Map<EventType, EventFactory> eventFactories;

    public static Processor constructEventMapProcessor(final Map<EventType, EventFactory> eventFactories) {
        return new ConstructEventMapProcessor(eventFactories);
    }

    @Override
    public void apply(final MetaData metaData) {
        final Object unmarshalled = metaData.getOptional(UNMARSHALLED_REQUEST_BODY).orElse(null);
        final EventType eventType = metaData.get(EVENT_TYPE);
        final EventFactory eventFactory = eventFactories.get(eventType);
        final EnrichableMap map = eventFactory.createEvent(unmarshalled);
        final Event event = event(map);
        metaData.set(EVENT, event);
    }
}
