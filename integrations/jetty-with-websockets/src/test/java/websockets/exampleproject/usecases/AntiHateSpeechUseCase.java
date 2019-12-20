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

package websockets.exampleproject.usecases;

import websockets.exampleproject.domain.Username;

import java.util.List;

import static de.quantummaid.messagemaid.processingContext.EventType.eventTypeFromString;
import static java.util.Arrays.asList;
import static websockets.exampleproject.Application.MESSAGE_BUS;
import static websockets.exampleproject.usecases.BanUserEvent.banUserEvent;

public final class AntiHateSpeechUseCase {
    private static final List<String> BLACKLIST = asList();

    private AntiHateSpeechUseCase() {
    }

    public static void register() {
        MESSAGE_BUS.subscribe(eventTypeFromString("SendMessageRequest"), o -> {
            final SendMessageRequest sendMessageRequest = (SendMessageRequest) o;
            final String content = sendMessageRequest.getContent().stringValue();
            final boolean hit = BLACKLIST.stream()
                    .map(content::contains)
                    .findFirst()
                    .orElse(false);
            if (hit) {
                final Username username = sendMessageRequest.getSender().username();
                MESSAGE_BUS.send(eventTypeFromString("BanUser"), banUserEvent(username));
            }
        });
    }
}
