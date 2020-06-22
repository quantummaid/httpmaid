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

import de.quantummaid.httpmaid.tests.givenwhenthen.client.shitty.ShittyClientFactory;
import de.quantummaid.httpmaid.tests.givenwhenthen.deploy.Deployer;

import java.util.List;

import static de.quantummaid.httpmaid.tests.deployers.DeployerManager.activeDeployers;
import static de.quantummaid.httpmaid.tests.deployers.DeployerManager.activeDeployersWithOnlyShittyClient;
import static de.quantummaid.httpmaid.tests.deployers.bypassed.BypassedDeployer.bypassedDeployer;
import static de.quantummaid.httpmaid.tests.deployers.fakeawslambda.FakeHttpApiGatewayV1PayloadDeployer.fakeHttpApiGatewayV1PayloadDeployer;
import static de.quantummaid.httpmaid.tests.deployers.fakeawslambda.FakeHttpApiGatewayV2PayloadDeployer.fakeHttpApiGatewayV2PayloadDeployer;
import static de.quantummaid.httpmaid.tests.deployers.fakeawslambda.FakeRestApiGatewayDeployer.fakeRestApiGatewayDeployer;
import static de.quantummaid.httpmaid.tests.deployers.jeeonundertow.JeeOnUndertowDeployer.jeeOnUndertowDeployer;
import static de.quantummaid.httpmaid.tests.deployers.jsr356ontyrus.Jsr356OnTyrusDeployer.programmaticJsr356OnTyrusDeployer;
import static de.quantummaid.httpmaid.tests.deployers.undertow.UndertowDeployer.undertowDeployer;
import static de.quantummaid.httpmaid.tests.givenwhenthen.TestEnvironment.testEnvironment;
import static java.util.stream.Collectors.toList;

public final class TestEnvironments {
    private static final String PACKAGE = "de.quantummaid.httpmaid.tests.givenwhenthen.TestEnvironments#";
    public static final String ALL_ENVIRONMENTS = PACKAGE + "allEnvironments";
    public static final String WEBSOCKET_ENVIRONMENTS = PACKAGE + "websocketEnvironments";
    public static final String WEBSOCKET_ENVIRONMENTS_WITHOUT_SHITTY_CLIENT = PACKAGE + "websocketEnvironmentsWithoutShittyClient";
    public static final String ENVIRONMENTS_WITH_ALL_CAPABILITIES = PACKAGE + "environmentsWithAllCapabilities";
    public static final String ONLY_SHITTY_CLIENT = PACKAGE + "onlyShittyClient";

    private TestEnvironments() {
    }

    public static List<TestEnvironment> websocketEnvironments() {
        final List<Deployer> deployers = List.of(
                bypassedDeployer(),
                fakeRestApiGatewayDeployer(),
                fakeHttpApiGatewayV2PayloadDeployer(),
                fakeHttpApiGatewayV1PayloadDeployer(),
                programmaticJsr356OnTyrusDeployer(),
                jeeOnUndertowDeployer(),
                undertowDeployer()
        );
        return deployers.stream()
                .flatMap(deployer -> deployer.supportedClients().stream()
                        .map(client -> testEnvironment(deployer, client)))
                .collect(toList());
    }

    public static List<TestEnvironment> websocketEnvironmentsWithoutShittyClient() {
        final List<Deployer> deployers = List.of(
                bypassedDeployer(),
                fakeRestApiGatewayDeployer(),
                fakeHttpApiGatewayV2PayloadDeployer(),
                fakeHttpApiGatewayV1PayloadDeployer(),
                programmaticJsr356OnTyrusDeployer(),
                jeeOnUndertowDeployer(),
                undertowDeployer()
        );
        return deployers.stream()
                .flatMap(deployer -> deployer.supportedClients().stream()
                        .filter(clientFactory -> !clientFactory.getClass().equals(ShittyClientFactory.class))
                        .map(client -> testEnvironment(deployer, client)))
                .collect(toList());
    }

    public static List<TestEnvironment> environmentsWithAllCapabilities() {
        final List<Deployer> deployers = List.of(
                bypassedDeployer(),
                jeeOnUndertowDeployer(),
                undertowDeployer()
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
}
