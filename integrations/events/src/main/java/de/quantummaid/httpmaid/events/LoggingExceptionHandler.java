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

package de.quantummaid.httpmaid.events;

import de.quantummaid.eventmaid.channel.Channel;
import de.quantummaid.eventmaid.messagebus.exception.MessageBusExceptionHandler;
import de.quantummaid.eventmaid.processingcontext.ProcessingContext;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class LoggingExceptionHandler implements MessageBusExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingExceptionHandler.class);

    public static LoggingExceptionHandler loggingExceptionHandler() {
        return new LoggingExceptionHandler();
    }

    @Override
    public boolean shouldDeliveryChannelErrorBeHandledAndDeliveryAborted(final ProcessingContext<Object> message,
                                                                         final Exception e,
                                                                         final Channel<Object> channel) {
        return true;
    }

    @Override
    public void handleDeliveryChannelException(final ProcessingContext<Object> message,
                                               final Exception e,
                                               final Channel<Object> channel) {
        LOGGER.error("Exception during event processing", e);
    }

    @Override
    public void handleFilterException(final ProcessingContext<Object> message,
                                      final Exception e,
                                      final Channel<Object> channel) {
        LOGGER.error("Exception during event processing", e);
    }
}
