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

package de.quantummaid.httpmaid.tests.givenwhenthen.builders;

import de.quantummaid.httpmaid.tests.givenwhenthen.Then;

import java.util.List;
import java.util.Map;

public interface FirstWhenStage {
    MethodBuilder aRequestToThePath(String path);

    default Then httpMaidIsInitialized() {
        return running(() -> {
        });
    }

    default Then aWebsocketIsTriedToBeConnected() {
        try {
            return aWebsocketIsConnected(Map.of(), Map.of());
        } catch (final Exception e) {
            return httpMaidIsInitialized();
        }
    }

    default Then aWebsocketIsConnected() {
        return aWebsocketIsConnected(Map.of(), Map.of());
    }

    default Then aWebsocketIsConnected(Map<String, List<String>> queryParameters, Map<String, List<String>> headers) {
        final int maxConnectionAttempts = 3;
        return aWebsocketIsConnected(queryParameters, headers, maxConnectionAttempts);
    }

    Then aWebsocketIsConnected(Map<String, List<String>> queryParameters,
                               Map<String, List<String>> headers,
                               int maxConnectionAttempts);

    Then running(Runnable runnable);

    Then aWebsocketMessageIsSent(String message);

    Then theLastWebsocketIsDisconnected();

    Then allWebsocketsAreDisconnected();

    Then theRuntimeDataIsQueriedUntilTheNumberOfWebsocketsBecomes(long expectedNumberOfWebsockets);

    Then theRuntimeDataIsQueried();
}
