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
import de.quantummaid.httpmaid.events.EventModule;
import de.quantummaid.httpmaid.events.enriching.PerEventEnrichers;
import de.quantummaid.httpmaid.handler.http.HttpRequest;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Map;

import static de.quantummaid.httpmaid.events.EventModule.EVENT_TYPE;
import static de.quantummaid.httpmaid.handler.http.HttpRequest.httpRequest;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class PerRequestEnrichersProcessor implements Processor {
    private final Map<EventType, PerEventEnrichers> enrichers;

    public static Processor enrichersProcessor(final Map<EventType, PerEventEnrichers> enrichers) {
        return new PerRequestEnrichersProcessor(enrichers);
    }

    @Override
    public void apply(final MetaData metaData) {
        metaData.getOptional(EVENT_TYPE).ifPresent(eventType -> {
            if (!enrichers.containsKey(eventType)) {
                return;
            }
            final Event event = metaData.get(EventModule.EVENT);
            final HttpRequest httpRequest = httpRequest(metaData);
            final PerEventEnrichers perEventEnrichers = enrichers.get(eventType);
            perEventEnrichers.enrich(httpRequest, event);
        });
    }
}
