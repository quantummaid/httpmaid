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

package de.quantummaid.httpmaid.websocketsusecases;

import de.quantummaid.messagemaid.processingContext.EventType;
import de.quantummaid.messagemaid.serializedMessageBus.SerializedMessageBus;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import static de.quantummaid.httpmaid.util.Validators.validateNotNull;
import static de.quantummaid.messagemaid.processingContext.EventType.eventTypeFromString;
import static java.util.UUID.randomUUID;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
final class WebSocketMessageSenderConfiguration {
    private final EventType eventType;
    private final SerializedMessageBus serializedMessageBus;

    static WebSocketMessageSenderConfiguration webSocketMessageSenderConfiguration(
            final SerializedMessageBus serializedMessageBus) {
        validateNotNull(serializedMessageBus, "serializedMessageBus");
        final EventType eventType = randomEventType();
        return new WebSocketMessageSenderConfiguration(eventType, serializedMessageBus);
    }

    EventType eventType() {
        return eventType;
    }

    SerializedMessageBus serializedMessageBus() {
        return serializedMessageBus;
    }

    private static EventType randomEventType() {
        final String uuid = randomUUID().toString();
        return eventTypeFromString(uuid);
    }
}
