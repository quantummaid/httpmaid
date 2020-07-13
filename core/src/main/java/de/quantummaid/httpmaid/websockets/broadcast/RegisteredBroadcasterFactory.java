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

package de.quantummaid.httpmaid.websockets.broadcast;

import de.quantummaid.reflectmaid.ResolvedType;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class RegisteredBroadcasterFactory {
    private final BroadcasterFactory<?, Object> factory;
    private final ResolvedType senderType;
    private final ResolvedType messageType;

    @SuppressWarnings("unchecked")
    public static <T, U> RegisteredBroadcasterFactory registeredBroadcasterFactory(
            final BroadcasterFactory<T, U> factory,
            final Class<T> senderType,
            final Class<U> messageType) {
        final ResolvedType resolvedSenderType = ResolvedType.resolvedType(senderType);
        final ResolvedType resolvedMessageType = ResolvedType.resolvedType(messageType);
        return new RegisteredBroadcasterFactory(
                (BroadcasterFactory<?, Object>) factory,
                resolvedSenderType,
                resolvedMessageType
        );
    }

    public BroadcasterFactory<?, Object> factory() {
        return factory;
    }

    public ResolvedType messageType() {
        return messageType;
    }

    public ResolvedType senderType() {
        return senderType;
    }
}
