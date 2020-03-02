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

package de.quantummaid.httpmaid.websocketsusecases;

import de.quantummaid.httpmaid.HttpMaid;
import de.quantummaid.httpmaid.generator.GenerationCondition;
import de.quantummaid.eventmaid.processingContext.EventType;
import de.quantummaid.eventmaid.serializedMessageBus.SerializedMessageBus;
import de.quantummaid.eventmaid.subscribing.AcceptingBehavior;
import de.quantummaid.eventmaid.subscribing.Subscriber;
import de.quantummaid.eventmaid.subscribing.SubscriptionId;
import de.quantummaid.eventmaid.useCases.payloadAndErrorPayload.PayloadAndErrorPayload;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Map;

import static de.quantummaid.httpmaid.usecases.UseCasesModule.SERIALIZED_MESSAGE_BUS;
import static de.quantummaid.httpmaid.util.Validators.validateNotNull;
import static de.quantummaid.httpmaid.websocketsusecases.WebSocketMessageSenderConfiguration.webSocketMessageSenderConfiguration;
import static de.quantummaid.eventmaid.subscribing.SubscriptionId.newUniqueId;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class WebSocketSenderFactory {
    private final HttpMaid httpMaid;
    private final SerializedMessageBus serializedMessageBus;

    public static WebSocketSenderFactory webSocketSenderFactoryFor(final HttpMaid httpMaid) {
        validateNotNull(httpMaid, "httpMaid");
        final SerializedMessageBus serializedMessageBus = httpMaid.getMetaDatum(SERIALIZED_MESSAGE_BUS);
        return new WebSocketSenderFactory(httpMaid, serializedMessageBus);
    }

    public void createWebsocketSenderThatSendsToWebsocketsThat(final GenerationCondition condition,
                                                               final WebSocketMessageSender sender) {
        validateNotNull(condition, "condition");
        validateNotNull(sender, "sender");

        final WebSocketMessageSenderConfiguration configuration = webSocketMessageSenderConfiguration(serializedMessageBus);
        final EventType eventType = configuration.eventType();

        serializedMessageBus.subscribe(eventType, new Subscriber<>() {
            @Override
            public AcceptingBehavior accept(final PayloadAndErrorPayload<Map<String, Object>, Map<String, Object>> message) {
                return null;
            }

            @Override
            public SubscriptionId getSubscriptionId() {
                return newUniqueId();
            }
        });

        sender.configure(configuration);
    }
}
