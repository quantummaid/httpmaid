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
import de.quantummaid.httpmaid.tests.givenwhenthen.deploy.Deployer;
import org.junit.jupiter.api.Test;

public interface RemoteSpecs {

    Deployer provideDeployer();

    // TODO trailing slash

    @Test
    default void httpTest(final TestEnvironment testEnvironment) {
        testEnvironment.givenTheStaticallyDeployedTestInstance()
                .when().aRequestToThePath("/").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theResponseBodyWas("fooooo");
    }

    @Test
    default void websocketTest(final TestEnvironment testEnvironment) {
        for (int i = 0; i < 3; ++i) {
            System.out.println("i = " + i);
            try {
                testEnvironment.givenTheStaticallyDeployedTestInstance()
                        .when().aWebsocketIsConnected()
                        .andWhen().aWebsocketMessageIsSent("{ \"message\": \"handler2\" }")
                        .aWebsocketMessageHasBeenReceivedWithContent("handler 2");
            } catch (Throwable e) {
                if (i > 1) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
