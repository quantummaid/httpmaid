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

package de.quantummaid.httpmaid.remotespecstomcat;

import de.quantummaid.httpmaid.client.HttpMaidClient;
import de.quantummaid.httpmaid.client.websocket.Websocket;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static de.quantummaid.httpmaid.client.HttpClientRequest.aGetRequestToThePath;
import static de.quantummaid.httpmaid.remotespecstomcat.BaseDirectoryFinder.findProjectBaseDirectory;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public final class RemoteSpecs {
    private static final String RELATIVE_PATH_TO_WAR = "/tests/war/target/remotespecs.war";
    private static final int PORT = 8080;
    private Tomcat tomcat;

    // TODO trailing slash

    @Test
    public void test() {
        final HttpMaidClient client = HttpMaidClient.aHttpMaidClientForTheHost("localhost").withThePort(PORT)
                .viaHttp().build();

        final String response = client.issue(aGetRequestToThePath("/").mappedToString());

        assertThat(response, is("fooooo"));
    }

    @Test
    public void test2() {
        final HttpMaidClient client = HttpMaidClient.aHttpMaidClientForTheHost("localhost").withThePort(PORT)
                .viaHttp().build();

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
        tomcat = new Tomcat();
        tomcat.setPort(PORT);
        final String basedir = "/home/marco/repositories/quantummaid/jacocotutorial/test";
        tomcat.setBaseDir(basedir);
        tomcat.getHost().setAppBase(basedir);
        tomcat.getHost().setAutoDeploy(true);
        tomcat.getHost().setDeployOnStartup(true);

        try {
            tomcat.start();
        } catch (final LifecycleException e) {
            throw new RuntimeException(e);
        }
        final String basePath = findProjectBaseDirectory();
        final String pathToWar = basePath + RELATIVE_PATH_TO_WAR;
        tomcat.addWebapp(tomcat.getHost(), "/", pathToWar);
    }

    @AfterAll
    public void cleanUp() {
        if (tomcat != null) {
            try {
                tomcat.stop();
            } catch (final LifecycleException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
