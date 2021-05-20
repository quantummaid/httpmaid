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

package de.quantummaid.httpmaid.remotespecs.lambda;

import de.quantummaid.httpmaid.tests.givenwhenthen.TestEnvironment;
import de.quantummaid.httpmaid.tests.givenwhenthen.Then;
import de.quantummaid.httpmaid.tests.givenwhenthen.WebsocketTestClientConnectException;
import de.quantummaid.httpmaid.tests.givenwhenthen.builders.FirstWhenStage;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

import static de.quantummaid.httpmaid.remotespecs.lambda.aws.dynamodb.DynamoDbAssertions.assertTableEmpty;
import static de.quantummaid.httpmaid.remotespecs.lambda.aws.dynamodb.DynamoDbAssertions.assertTableHasNumberOfEntries;
import static de.quantummaid.httpmaid.remotespecs.lambda.aws.dynamodb.DynamoDbHandler.resetTable;
import static java.lang.String.format;

@Slf4j
public final class Shared {
    private static final int MAX_ATTEMPTS = 5;

    private Shared() {
    }

    public static void noLeakedConnectionsInWebsocketRegistryAfterDisconnectByClient(final TestEnvironment testEnvironment,
                                                                                     final String websocketRegistryDynamoDb,
                                                                                     final Map<String, List<String>> mapWithAccessToken) {
        noLeakedConnectionsInWebsocketRegistryAfterDisconnectByClient(
                testEnvironment,
                websocketRegistryDynamoDb,
                mapWithAccessToken,
                "disconnectByClient"
        );
    }

    private static void noLeakedConnectionsInWebsocketRegistryAfterDisconnectByClient(final TestEnvironment testEnvironment,
                                                                                      final String websocketRegistryDynamoDb,
                                                                                      final Map<String, List<String>> mapWithAccessToken,
                                                                                      final String traceId) {
        final Then connectedStage = cleanUpAndConnectWebsockets(websocketRegistryDynamoDb, testEnvironment, mapWithAccessToken, traceId, 5);
        assertTableHasNumberOfEntries(5, websocketRegistryDynamoDb);

        connectedStage
                .andWhen().allWebsocketsAreDisconnected()
                .allWebsocketsHaveBeenClosed();

        assertTableEmpty(websocketRegistryDynamoDb);
    }

    public static void noLeakedConnectionsInWebsocketRegistryAfterDisconnectByServer(final TestEnvironment testEnvironment,
                                                                                     final String websocketRegistryDynamoDb,
                                                                                     final Map<String, List<String>> mapWithAccessToken) {
        noLeakedConnectionsInWebsocketRegistryAfterDisconnectByServer(
                testEnvironment,
                websocketRegistryDynamoDb,
                mapWithAccessToken,
                "disconnectByServer"
        );
    }

    private static void noLeakedConnectionsInWebsocketRegistryAfterDisconnectByServer(final TestEnvironment testEnvironment,
                                                                                      final String websocketRegistryDynamoDb,
                                                                                      final Map<String, List<String>> mapWithAccessToken,
                                                                                      final String traceId) {
        final Then connectedStage = cleanUpAndConnectWebsockets(websocketRegistryDynamoDb, testEnvironment, mapWithAccessToken, traceId, 5);

        assertTableHasNumberOfEntries(5, websocketRegistryDynamoDb);

        connectedStage
                .andWhen().aWebsocketMessageIsSent("{ \"message\": \"disconnect\" }")
                .allWebsocketsHaveBeenClosed();

        assertTableEmpty(websocketRegistryDynamoDb);
    }

    private static Then cleanUpAndConnectWebsockets(final String websocketRegistryDynamoDb,
                                                    final TestEnvironment testEnvironment,
                                                    final Map<String, List<String>> mapWithAccessToken,
                                                    final String traceId,
                                                    final int number) {
        int count = 0;
        Then connectedStage;
        while (true) {
            if (count > MAX_ATTEMPTS) {
                throw new IllegalStateException(format("tried to connect websockets %d times, giving up.", MAX_ATTEMPTS));
            }

            resetTable(websocketRegistryDynamoDb);
            assertTableEmpty(websocketRegistryDynamoDb);

            try {
                connectedStage = connectWebsockets(number, testEnvironment, mapWithAccessToken, traceId);
                break;
            } catch (final WebsocketTestClientConnectException e) {
                log.warn("could not connect all websockets", e);
                count++;
            }
        }
        return connectedStage;
    }

    private static Then connectWebsockets(final int number,
                                          final TestEnvironment testEnvironment,
                                          final Map<String, List<String>> mapWithAccessToken,
                                          final String traceId) {
        final FirstWhenStage when = testEnvironment.givenTheStaticallyDeployedTestInstance()
                .when();
        Then connectedStage = null;
        for (int i = 0; i < number; ++i) {
            connectedStage = when.aWebsocketIsConnected(mapWithAccessToken, Map.of("X-Trace-Id", List.of(traceId)), 1);
        }
        return connectedStage;
    }
}
