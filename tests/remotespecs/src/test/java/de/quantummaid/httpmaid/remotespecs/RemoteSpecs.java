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

import de.quantummaid.httpmaid.client.HttpMaidClient;
import de.quantummaid.httpmaid.client.websocket.Websocket;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;

import static de.quantummaid.httpmaid.client.HttpClientRequest.aGetRequestToThePath;
import static de.quantummaid.httpmaid.client.HttpMaidClient.aHttpMaidClientForTheHost;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public final class RemoteSpecs {
    private static final String RELATIVE_PATH_TO_WAR = "/tests/lamda/target/remotespecs.jar";
    private static final int HTTPS_PORT = 443;

    // TODO trailing slash

    //@Test
    public void test() {
        // https://4zwrbier78.execute-api.eu-west-1.amazonaws.com/foo

        final HttpMaidClient client = aHttpMaidClientForTheHost("4zwrbier78.execute-api.eu-west-1.amazonaws.com")
                .withThePort(HTTPS_PORT)
                .viaHttps()
                .build();

        final String response = client.issue(aGetRequestToThePath("/foo").mappedToString());

        assertThat(response, is("fooooo"));
    }

    //@Test
    public void test2() {
        // wss://5l2betejm3.execute-api.eu-west-1.amazonaws.com/foo

        final HttpMaidClient client = aHttpMaidClientForTheHost("5l2betejm3.execute-api.eu-west-1.amazonaws.com")
                .withThePort(HTTPS_PORT)
                .viaHttps()
                .withBasePath("/foo")
                .build();

        final Websocket websocket = client.openWebsocket(System.out::println);
        websocket.send("{ \"message\": \"handler2\" }");

        /*
        try {
            Thread.sleep(10000);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
        }
         */
    }

    @BeforeAll
    public void deploy() {
    }

    @AfterAll
    public void cleanUp() {
    }
}
