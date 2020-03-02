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

package de.quantummaid.httpmaid.websocketsevents;

import de.quantummaid.httpmaid.chains.MetaData;
import de.quantummaid.httpmaid.chains.Processor;
import de.quantummaid.httpmaid.util.Validators;
import de.quantummaid.eventmaid.processingContext.EventType;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.List;
import java.util.Optional;

import static de.quantummaid.httpmaid.events.EventModule.EVENT_TYPE;
import static de.quantummaid.httpmaid.websockets.WebsocketChainKeys.IS_WEBSOCKET_MESSAGE;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class DetermineEventForWebSocketsProcessor implements Processor {
    private final List<EventMapping> eventMappings;

    public static Processor determineEventForWebSockets(final List<EventMapping> eventMappings) {
        Validators.validateNotNull(eventMappings, "eventMappings");
        return new DetermineEventForWebSocketsProcessor(eventMappings);
    }

    @Override
    public void apply(final MetaData metaData) {
        if (metaData.getOptional(IS_WEBSOCKET_MESSAGE).orElse(false)) {
            final EventType eventType = eventMappings.stream()
                    .map(useCaseMapping -> useCaseMapping.getEventIfMatching(metaData))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("No event found"));
            metaData.set(EVENT_TYPE, eventType);
        }
    }
}
