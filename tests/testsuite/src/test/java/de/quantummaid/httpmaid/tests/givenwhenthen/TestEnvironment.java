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
import de.quantummaid.httpmaid.tests.givenwhenthen.client.ClientFactory;
import de.quantummaid.httpmaid.tests.givenwhenthen.deploy.Deployer;
import de.quantummaid.httpmaid.tests.givenwhenthen.remote.warontomcat.WarOnTomcatDeployer;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.List;
import java.util.function.Supplier;

import static de.quantummaid.httpmaid.tests.givenwhenthen.deploy.DeployerManager.activeDeployers;
import static de.quantummaid.httpmaid.tests.givenwhenthen.deploy.DeployerManager.activeDeployersWithOnlyShittyClient;
import static de.quantummaid.httpmaid.tests.givenwhenthen.deploy.bypassed.BypassedDeployer.bypassedDeployer;
import static de.quantummaid.httpmaid.tests.givenwhenthen.deploy.fakeawslambda.websocket.WebsocketDeployer.websocketDeployer;
import static de.quantummaid.httpmaid.tests.givenwhenthen.deploy.jsr356ontyrus.ProgrammaticJsr356OnTyrusDeployer.programmaticJsr356OnTyrusDeployer;
import static de.quantummaid.httpmaid.util.Validators.validateNotNull;
import static java.util.stream.Collectors.toList;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class TestEnvironment {
    private static final String PACKAGE = "de.quantummaid.httpmaid.tests.givenwhenthen.TestEnvironment#";
    public static final String ALL_ENVIRONMENTS = PACKAGE + "allEnvironments";
    public static final String WEBSOCKET_ENVIRONMENTS = PACKAGE + "websocketEnvironments";
    public static final String REMOTE_ENVIRONMENTS = PACKAGE + "remoteEnvironments";
    public static final String ONLY_SHITTY_CLIENT = PACKAGE + "onlyShittyClient";

    private final Deployer deployer;
    private final ClientFactory clientFactory;

    public static TestEnvironment testEnvironment(final Deployer deployer,
                                                  final ClientFactory clientFactory) {
        validateNotNull(deployer, "deployer");
        validateNotNull(clientFactory, "clientFactory");
        return new TestEnvironment(deployer, clientFactory);
    }

    public static List<TestEnvironment> remoteEnvironments() {
        final List<Deployer> deployers = List.of(
                WarOnTomcatDeployer.warOnTomcatDeployer()
        );
        return deployers.stream()
                .flatMap(deployer -> deployer.supportedClients().stream()
                        .map(client -> testEnvironment(deployer, client)))
                .collect(toList());
    }

    public static List<TestEnvironment> websocketEnvironments() {
        final List<Deployer> deployers = List.of(
                bypassedDeployer(),
                websocketDeployer(),
                programmaticJsr356OnTyrusDeployer()
        );
        return deployers.stream()
                .flatMap(deployer -> deployer.supportedClients().stream()
                        .map(client -> testEnvironment(deployer, client)))
                .collect(toList());
    }

    public static List<TestEnvironment> allEnvironments() {
        return activeDeployers()
                .stream()
                .map(deployerAndClient -> testEnvironment(deployerAndClient.deployer(), deployerAndClient.clientFactory()))
                .collect(toList());
    }

    public static List<TestEnvironment> onlyShittyClient() {
        return activeDeployersWithOnlyShittyClient()
                .stream()
                .map(deployerAndClient -> testEnvironment(deployerAndClient.deployer(), deployerAndClient.clientFactory()))
                .collect(toList());
    }

    public Given given(final HttpMaidSupplier httpMaidSupplier) {
        return Given.given(httpMaidSupplier, deployer, clientFactory);
    }

    public Given given(final Supplier<HttpMaid> httpMaidSupplier) {
        return given(checkpoints -> httpMaidSupplier.get());
    }

    public Given given(final HttpMaid httpMaid) {
        return given(() -> httpMaid);
    }
}
