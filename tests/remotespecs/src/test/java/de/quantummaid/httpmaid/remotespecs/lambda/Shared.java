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
import de.quantummaid.httpmaid.tests.givenwhenthen.builders.FirstWhenStage;

import java.util.List;
import java.util.Map;

import static de.quantummaid.httpmaid.remotespecs.lambda.aws.dynamodb.DynamoDbAssertions.assertTableEmpty;
import static de.quantummaid.httpmaid.remotespecs.lambda.aws.dynamodb.DynamoDbAssertions.assertTableHasNumberOfEntries;
import static de.quantummaid.httpmaid.remotespecs.lambda.aws.dynamodb.DynamoDbHandler.resetTable;

public final class Shared {
    private Shared() {
    }

    public static void noLeakedConnectionsInWebsocketRegistryAfterDisconnectByClient(final TestEnvironment testEnvironment,
                                                                                     final String websocketRegistryDynamoDb,
                                                                                     final Map<String, List<String>> mapWithAccessToken) {
        resetTable(websocketRegistryDynamoDb);
        assertTableEmpty(websocketRegistryDynamoDb);

        final Then connectedStage = connectWebsockets(20, testEnvironment, mapWithAccessToken);

        assertTableHasNumberOfEntries(20, websocketRegistryDynamoDb);

        connectedStage
                .andWhen().allWebsocketsAreDisconnected()
                .allWebsocketsHaveBeenClosed();

        assertTableEmpty(websocketRegistryDynamoDb);
    }

    public static void noLeakedConnectionsInWebsocketRegistryAfterDisconnectByServer(final TestEnvironment testEnvironment,
                                                                                     final String websocketRegistryDynamoDb,
                                                                                     final Map<String, List<String>> mapWithAccessToken) {
        resetTable(websocketRegistryDynamoDb);
        assertTableEmpty(websocketRegistryDynamoDb);

        final Then connectedStage = connectWebsockets(20, testEnvironment, mapWithAccessToken);

        assertTableHasNumberOfEntries(20, websocketRegistryDynamoDb);

        connectedStage
                .andWhen().aWebsocketMessageIsSent("{ \"message\": \"disconnect\" }")
                .allWebsocketsHaveBeenClosed();

        assertTableEmpty(websocketRegistryDynamoDb);
    }

    private static Then connectWebsockets(final int number,
                                          final TestEnvironment testEnvironment,
                                          final Map<String, List<String>> mapWithAccessToken) {
        final FirstWhenStage when = testEnvironment.givenTheStaticallyDeployedTestInstance()
                .when();
        Then connectedStage = null;
        for (int i = 0; i < number; ++i) {
            connectedStage = when.aWebsocketIsConnected(mapWithAccessToken, Map.of());
        }
        return connectedStage;
    }
}
