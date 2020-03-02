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

import de.quantummaid.httpmaid.chains.MetaData;
import de.quantummaid.httpmaid.chains.Processor;
import de.quantummaid.eventmaid.messageBus.MessageBus;
import de.quantummaid.eventmaid.messageFunction.MessageFunction;
import de.quantummaid.eventmaid.messageFunction.ResponseFuture;
import de.quantummaid.eventmaid.processingContext.EventType;
import de.quantummaid.eventmaid.processingContext.ProcessingContext;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Map;
import java.util.concurrent.ExecutionException;

import static de.quantummaid.httpmaid.events.EventModule.*;
import static de.quantummaid.httpmaid.events.processors.EventDispatchingException.eventDispatchingException;
import static de.quantummaid.eventmaid.messageFunction.MessageFunctionBuilder.aMessageFunction;
import static java.lang.Thread.currentThread;
import static java.util.Optional.ofNullable;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class DispatchEventProcessor implements Processor {
    private final MessageFunction messageFunction;

    public static Processor dispatchEventProcessor(final MessageBus messageBus) {
        final MessageFunction messageFunction = aMessageFunction(messageBus);
        return new DispatchEventProcessor(messageFunction);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void apply(final MetaData metaData) {
        final EventType eventType = metaData.get(EVENT_TYPE);
        final Object event = metaData.get(EVENT);
        final ResponseFuture request = messageFunction.request(eventType, event);
        try {
            final ProcessingContext<Map<String, Object>> raw = (ProcessingContext<Map<String, Object>>) (Object) request.getRaw();
            if (raw.getErrorPayload() != null) {
                final Map<String, Object> errorPayload = (Map<String, Object>) raw.getErrorPayload();
                final Throwable exception = (Throwable) errorPayload.get("Exception");
                throw eventDispatchingException(exception);
            } else {
                final Map<String, Object> response = raw.getPayload();
                metaData.set(RECEIVED_EVENT, ofNullable(response));
            }
        } catch (final InterruptedException e) {
            request.cancel(true);
            currentThread().interrupt();
        } catch (final ExecutionException e) {
            throw eventDispatchingException(e.getCause());
        }
    }
}
