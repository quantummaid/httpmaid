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

package de.quantummaid.httpmaid.tests.givenwhenthen.checkpoints;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Thread.currentThread;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Checkpoints {
    private static final int WAIT_TIME = 120;
    private static final int SLEEP_TIME = 250;

    private final List<String> checkpoints = new ArrayList<>(0);

    public static Checkpoints checkpoints() {
        return new Checkpoints();
    }

    public synchronized void visitCheckpoint(final String checkpoint) {
        checkpoints.add(checkpoint);
    }

    private synchronized boolean hasCheckpointBeenVisited(final String checkpoint) {
        return checkpoints.contains(checkpoint);
    }

    public boolean checkpointHasBeenVisited(final String checkpoint) {
        for (int i = 0; i < WAIT_TIME; ++i) {
            if (hasCheckpointBeenVisited(checkpoint)) {
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
