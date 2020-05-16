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

package de.quantummaid.httpmaid.tests.givenwhenthen;

import java.util.function.Supplier;

import static java.lang.Thread.currentThread;

public final class Poller {
    private static final int WAIT_TIME = 60 * 4;
    private static final int SLEEP_TIME = 250;

    private Poller() {
    }

    public static boolean pollWithTimeout(final Supplier<Boolean> condition) {
        for (int i = 0; i < WAIT_TIME; ++i) {
            final Boolean conditionHasBeenFullfilled = condition.get();
            if (conditionHasBeenFullfilled) {
                return true;
            }
            try {
                Thread.sleep(SLEEP_TIME);
            } catch (final InterruptedException e) {
                currentThread().interrupt();
            }
        }
        return false;
    }
}
