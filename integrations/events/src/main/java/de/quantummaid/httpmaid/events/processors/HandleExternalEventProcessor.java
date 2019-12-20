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

package de.quantummaid.httpmaid.events.processors;

import de.quantummaid.httpmaid.events.ExternalEventMapping;
import de.quantummaid.httpmaid.chains.MetaData;
import de.quantummaid.httpmaid.chains.Processor;
import de.quantummaid.messagemaid.processingContext.EventType;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Map;

import static de.quantummaid.httpmaid.events.EventModule.EVENT_TYPE;
import static de.quantummaid.httpmaid.util.Validators.validateNotNull;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class HandleExternalEventProcessor implements Processor {
    private final Map<EventType, ExternalEventMapping> mappings;

    public static Processor handleExternalEventProcessor(final Map<EventType, ExternalEventMapping> mappings) {
        validateNotNull(mappings, "mappings");
        return new HandleExternalEventProcessor(mappings);
    }

    @Override
    public void apply(final MetaData metaData) {
        final EventType eventType = metaData.get(EVENT_TYPE);
        final ExternalEventMapping mapping = mappings.get(eventType);
        validateNotNull(mapping, "mapping");
        mapping.apply(metaData);
    }
}
