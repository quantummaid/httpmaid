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

package de.quantummaid.httpmaid.remotespecs.jar;

import de.quantummaid.httpmaid.HttpMaid;
import de.quantummaid.httpmaid.remotespecs.BaseDirectoryFinder;
import de.quantummaid.httpmaid.tests.givenwhenthen.Poller;
import de.quantummaid.httpmaid.tests.givenwhenthen.client.ClientFactory;
import de.quantummaid.httpmaid.tests.givenwhenthen.deploy.Deployment;
import de.quantummaid.httpmaid.tests.givenwhenthen.deploy.PortDeployer;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import static de.quantummaid.httpmaid.tests.givenwhenthen.deploy.DeploymentBuilder.deploymentBuilder;
import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;

@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class JarDeployer implements PortDeployer {
    private static final String RELATIVE_PATH_TO_JAR = "/tests/jar/target/remotespecs.jar";
    private static final String READY_MESSAGE = "TestJar ready to be tested.";

    private Process process;

    public static JarDeployer jarDeployer() {
        return new JarDeployer();
    }

    @Override
    public Deployment deploy(final int port,
                             final HttpMaid httpMaid) {
        final String projectBaseDirectory = BaseDirectoryFinder.findProjectBaseDirectory();
        final String jarPath = projectBaseDirectory + RELATIVE_PATH_TO_JAR;
        final String command = format("java -jar %s %d", jarPath, port);
        try {
            process = Runtime.getRuntime().exec(command);
            waitForEndpointToBecomeAvailable();
            return deploymentBuilder()
                    .withHttpPort(port)
                    .withWebsocketPort(port)
                    .build();
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void waitForEndpointToBecomeAvailable() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), UTF_8))) {
            final StringBuilder stringBuilder = new StringBuilder();
            final boolean ready = Poller.pollWithTimeout(() -> {
                try {
                    while (reader.ready()) {
                        final String line = reader.readLine();
                        stringBuilder.append(line);
                        if (READY_MESSAGE.equals(line)) {
                            return true;
                        }
                    }
                    return false;
                } catch (final IOException e) {
                    throw new RuntimeException(e);
                }
            });
            if (!ready) {
                process.destroy();
                throw new RuntimeException(format(
                        "JAR endpoint did not become available in time. Output:%n%s",
                        stringBuilder.toString())
                );
            }
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void cleanUp() {
        if (process != null) {
            process.destroy();
        }
    }

    @Override
    public List<ClientFactory> supportedClients() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return "jar";
    }
}
