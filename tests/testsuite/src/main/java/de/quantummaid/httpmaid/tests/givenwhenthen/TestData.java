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

import de.quantummaid.httpmaid.HttpMaid;
import de.quantummaid.httpmaid.RuntimeInformation;
import de.quantummaid.httpmaid.tests.givenwhenthen.checkpoints.Checkpoints;
import de.quantummaid.httpmaid.tests.givenwhenthen.client.HttpClientResponse;
import de.quantummaid.httpmaid.tests.givenwhenthen.client.HttpClientWrapper;
import de.quantummaid.httpmaid.tests.givenwhenthen.websockets.Websockets;
import lombok.*;

import static de.quantummaid.httpmaid.tests.givenwhenthen.websockets.Websockets.emptyWebsockets;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class TestData {
    @Getter
    private final TestEnvironment testEnvironment;
    @Getter
    @Setter
    private HttpClientResponse response;
    @Getter
    @Setter
    private Throwable initializationException;
    @Getter
    @Setter
    private Checkpoints checkpoints;
    @Getter
    @Setter
    private HttpClientWrapper clientWrapper;
    @Getter
    private final Websockets websockets = emptyWebsockets();
    @Getter
    @Setter
    private HttpMaid httpMaid;
    @Getter
    @Setter
    private RuntimeInformation runtimeInformation;

    public static TestData testData(final TestEnvironment testEnvironment) {
        return new TestData(testEnvironment);
    }
}
