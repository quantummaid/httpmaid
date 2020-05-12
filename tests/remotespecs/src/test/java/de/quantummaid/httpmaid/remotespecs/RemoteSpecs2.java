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

package de.quantummaid.httpmaid.remotespecs;

import de.quantummaid.httpmaid.tests.givenwhenthen.TestEnvironment;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static de.quantummaid.httpmaid.remotespecs.deployers.TestEnvironments.ALL_ENVIRONMENTS;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public final class RemoteSpecs2 {

    // TODO trailing slash

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void httpTest(final TestEnvironment testEnvironment) {
        testEnvironment.givenTheStaticallyDeployedTestInstance()
                .when().aRequestToThePath("/").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theResponseBodyWas("fooooo");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void websocketTest(final TestEnvironment testEnvironment) {
        testEnvironment.givenTheStaticallyDeployedTestInstance()
                .when().aWebsocketIsConnectedAndMessageSent("{ \"message\": \"handler2\" }")
                .aWebsocketMessageHasBeenReceivedWithContent("handler 2");
    }
}
