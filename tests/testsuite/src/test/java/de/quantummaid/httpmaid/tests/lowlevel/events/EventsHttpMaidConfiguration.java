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

package de.quantummaid.httpmaid.tests.lowlevel.events;

import de.quantummaid.httpmaid.HttpMaid;
import de.quantummaid.eventmaid.identification.CorrelationId;
import de.quantummaid.eventmaid.messageBus.MessageBus;
import de.quantummaid.eventmaid.messageBus.MessageBusType;

import java.util.HashMap;

import static de.quantummaid.httpmaid.HttpMaid.anHttpMaid;
import static de.quantummaid.httpmaid.events.EventConfigurators.toUseTheMessageBus;
import static de.quantummaid.eventmaid.configuration.AsynchronousConfiguration.constantPoolSizeAsynchronousConfiguration;
import static de.quantummaid.eventmaid.messageBus.MessageBusBuilder.aMessageBus;
import static de.quantummaid.eventmaid.processingContext.EventType.eventTypeFromString;

final class EventsHttpMaidConfiguration {
    private static MessageBus messageBus;

    private EventsHttpMaidConfiguration() {
    }

    static HttpMaid theEventsHttpMaidInstanceUsedForTesting() {
        messageBus = aMessageBus()
                .forType(MessageBusType.ASYNCHRONOUS)
                .withAsynchronousConfiguration(constantPoolSizeAsynchronousConfiguration(4))
                .build();

        final HttpMaid httpMaid = anHttpMaid()
                .get("/trigger", eventTypeFromString("trigger"))
                .configured(toUseTheMessageBus(messageBus))
                .build();

        messageBus.subscribeRaw(eventTypeFromString("trigger"), processingContext -> {
            final CorrelationId correlationId = processingContext.generateCorrelationIdForAnswer();
            messageBus.send(eventTypeFromString("answer"), new HashMap<>(), correlationId);
        });

        return httpMaid;
    }
}
