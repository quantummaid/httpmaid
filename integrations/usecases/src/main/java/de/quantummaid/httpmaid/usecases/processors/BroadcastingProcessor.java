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
import de.quantummaid.httpmaid.usecases.eventfactories.enriching.Event;
import de.quantummaid.httpmaid.websockets.broadcast.Broadcasters;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.List;

import static de.quantummaid.httpmaid.usecases.UseCasesModule.EVENT;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class BroadcastingProcessor implements Processor {
    private final Broadcasters broadcasters;

    public static BroadcastingProcessor broadcastingProcessor(final Broadcasters broadcasters) {
        return new BroadcastingProcessor(broadcasters);
    }

    @Override
    public void apply(final MetaData metaData) {
        final Event event = metaData.get(EVENT);
        final List<Object> broadcasterInstances = broadcasters.instantiateAll(metaData);
        broadcasterInstances.forEach(event::addTypeInjection);
    }
}
