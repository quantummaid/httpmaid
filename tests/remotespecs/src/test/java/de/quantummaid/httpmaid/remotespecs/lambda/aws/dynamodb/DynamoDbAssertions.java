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

import static de.quantummaid.httpmaid.remotespecs.lambda.aws.dynamodb.DynamoDbHandler.countEntries;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public final class DynamoDbAssertions {
    private static final int MAX_NUMBER_OF_TRIES = 120;
    private static final int SLEEP_TIME_IN_MILLISECONDS = 1000;

    private DynamoDbAssertions() {
    }

    public static void assertTableEmpty(final String tableName) {
        assertTableHasNumberOfEntries(0, tableName);
    }

    public static void assertTableHasNumberOfEntries(final int numberOfEntries, final String tableName) {
        Poller.pollWithTimeout(MAX_NUMBER_OF_TRIES, SLEEP_TIME_IN_MILLISECONDS, () -> {
            final int entries = countEntries(tableName);
            return entries == numberOfEntries;
        });

        final int entries = countEntries(tableName);
        assertThat(entries, is(numberOfEntries));
    }
}
