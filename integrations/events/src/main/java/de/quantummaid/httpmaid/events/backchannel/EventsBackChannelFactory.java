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

package de.quantummaid.httpmaid.events.backchannel;

import de.quantummaid.httpmaid.backchannel.BackChannelFactory;
import de.quantummaid.httpmaid.backchannel.BackChannelTrigger;
import de.quantummaid.eventmaid.messageBus.MessageBus;
import de.quantummaid.eventmaid.processingContext.EventType;
import de.quantummaid.eventmaid.subscribing.AcceptingBehavior;
import de.quantummaid.eventmaid.subscribing.Subscriber;
import de.quantummaid.eventmaid.subscribing.SubscriptionId;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import static de.quantummaid.httpmaid.util.Validators.validateNotNull;
import static de.quantummaid.eventmaid.processingContext.EventType.eventTypeFromString;
import static de.quantummaid.eventmaid.subscribing.AcceptingBehavior.MESSAGE_ACCEPTED;
import static de.quantummaid.eventmaid.subscribing.SubscriptionId.newUniqueId;
import static java.util.UUID.randomUUID;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class EventsBackChannelFactory implements BackChannelFactory {
    private final MessageBus messageBus;

    @Override
    public BackChannelTrigger createTrigger(final Runnable action) {
        validateNotNull(action, "action");

        final EventType eventType = randomEventType();

        messageBus.subscribe(eventType, new Subscriber<>() {
            @Override
            public AcceptingBehavior accept(final Object message) {
                action.run();
                return MESSAGE_ACCEPTED;
            }

            @Override
            public SubscriptionId getSubscriptionId() {
                return newUniqueId();
            }
        });

        return () -> messageBus.send(eventType, new Object());
    }

    private static EventType randomEventType() {
        final String uuid = randomUUID().toString();
        return eventTypeFromString(uuid);
    }
}
