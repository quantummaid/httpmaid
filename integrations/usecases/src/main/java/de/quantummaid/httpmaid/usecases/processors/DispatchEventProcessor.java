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
import de.quantummaid.usecasemaid.RoutingTarget;
import de.quantummaid.usecasemaid.UseCaseMaid;
import de.quantummaid.usecasemaid.UseCaseResult;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Map;
import java.util.Optional;

import static de.quantummaid.httpmaid.usecases.UseCasesModule.*;
import static de.quantummaid.httpmaid.usecases.processors.EventDispatchingException.eventDispatchingException;
import static de.quantummaid.httpmaid.util.Validators.validateNotNull;
import static de.quantummaid.usecasemaid.InvocationId.randomInvocationId;
import static java.util.Optional.ofNullable;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class DispatchEventProcessor implements Processor {
    private final UseCaseMaid useCaseMaid;

    public static Processor dispatchEventProcessor(final UseCaseMaid useCaseMaid) {
        validateNotNull(useCaseMaid, "useCaseMaid");
        return new DispatchEventProcessor(useCaseMaid);
    }

    @Override
    public void apply(final MetaData metaData) {
        final RoutingTarget routingTarget = metaData.get(ROUTING_TARGET);
        final Event event = metaData.get(EVENT);
        final Map<String, Object> eventMap = event.asMap();
        final UseCaseResult useCaseResult = useCaseMaid.invoke(
                routingTarget,
                eventMap,
                randomInvocationId(),
                null,
                injector -> {
                    event.injections().forEach(injection -> {
                        final String key = injection.key();
                        final String value = injection.value();
                        injector.put(key, value);
                    });
                    event.typeInjections().forEach(injector::put);
                });
        if (!useCaseResult.wasSuccessful()) {
            final Throwable exception = useCaseResult.exception();
            throw eventDispatchingException(exception);
        }
        if (useCaseResult.hasReturnValue()) {
            final Object response = useCaseResult.returnValue();
            metaData.set(RECEIVED_EVENT, ofNullable(response));
        } else {
            metaData.set(RECEIVED_EVENT, Optional.empty());
        }
    }
}
