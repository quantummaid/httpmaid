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

package de.quantummaid.httpmaid.remotespecs.lambda.aws.dynamodb;

import de.quantummaid.httpmaid.tests.givenwhenthen.Poller;

import java.util.List;
import java.util.Map;

import static de.quantummaid.httpmaid.remotespecs.lambda.aws.dynamodb.DynamoDbHandler.entries;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

public final class DynamoDbAssertions {
    private static final int MAX_NUMBER_OF_TRIES = 60;
    private static final int SLEEP_TIME_IN_MILLISECONDS = 1000;

    private DynamoDbAssertions() {
    }

    public static void assertTableEmpty(final String tableName) {
        assertTableHasNumberOfEntries(0, tableName);
    }

    public static void assertTableHasNumberOfEntries(final int numberOfEntries, final String tableName) {
        Poller.pollWithTimeout(MAX_NUMBER_OF_TRIES, SLEEP_TIME_IN_MILLISECONDS, () -> {
            final int entries = entries(tableName).size();
            return entries == numberOfEntries;
        });

        final List<Map<String, Object>> entries = entries(tableName);
        assertThat(entries.toString(), entries, hasSize(numberOfEntries));
    }
}
