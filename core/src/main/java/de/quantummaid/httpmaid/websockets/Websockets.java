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

package de.quantummaid.httpmaid.websockets;

import de.quantummaid.httpmaid.websockets.broadcast.SerializingSender;
import de.quantummaid.httpmaid.websockets.disconnect.Disconnector;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Websockets {
    private final SerializingSender<Object> sender;
    private final Disconnector disconnector;

    public static Websockets websockets(final SerializingSender<Object> sender,
                                        final Disconnector disconnector) {
        return new Websockets(sender, disconnector);
    }

    public SerializingSender<Object> sender() {
        return sender;
    }

    public Disconnector disconnector() {
        return disconnector;
    }
}