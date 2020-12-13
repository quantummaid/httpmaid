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

package de.quantummaid.httpmaid.tests.deployers;

import de.quantummaid.httpmaid.tests.givenwhenthen.DeployerAndClient;
import de.quantummaid.httpmaid.tests.givenwhenthen.client.shitty.ShittyClientFactory;
import de.quantummaid.httpmaid.tests.givenwhenthen.deploy.Deployer;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import static de.quantummaid.httpmaid.tests.deployers.bypassed.BypassedDeployer.bypassedDeployer;
import static de.quantummaid.httpmaid.tests.deployers.fakeawslambda.FakeHttpApiGatewayV1PayloadDeployer.fakeHttpApiGatewayV1PayloadDeployer;
import static de.quantummaid.httpmaid.tests.deployers.fakeawslambda.FakeHttpApiGatewayV1PayloadWithWsAuthDeployer.fakeHttpApiGatewayV1PayloadWithWsAuthDeployer;
import static de.quantummaid.httpmaid.tests.deployers.fakeawslambda.FakeHttpApiGatewayV2PayloadDeployer.fakeHttpApiGatewayV2PayloadDeployer;
import static de.quantummaid.httpmaid.tests.deployers.fakeawslambda.FakeHttpApiGatewayV2PayloadWithWsAuthDeployer.fakeHttpApiGatewayV2PayloadWithWsAuthDeployer;
import static de.quantummaid.httpmaid.tests.deployers.fakeawslambda.FakeRestApiGatewayDeployer.fakeRestApiGatewayDeployer;
import static de.quantummaid.httpmaid.tests.deployers.fakeawslambda.FakeRestApiGatewayWithWsAuthorizationDeployer.fakeRestApiGatewayWithWsAuthorizationDeployer;
import static de.quantummaid.httpmaid.tests.deployers.jetty.JettyDeployer.jettyDeployer;
import static de.quantummaid.httpmaid.tests.deployers.jettywebsockets.JettyWebsocketsDeployer.jettyWebsocketsDeployer;
import static de.quantummaid.httpmaid.tests.deployers.purejava.PureJavaDeployer.pureJavaDeployer;
import static de.quantummaid.httpmaid.tests.deployers.servletonjetty.ServletOnJettyDeployer.servletOnJettyDeployer;
import static de.quantummaid.httpmaid.tests.givenwhenthen.DeployerAndClient.deployerAndClient;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

public final class DeployerManager {
    private static final Collection<Deployer> ACTIVE_DEPLOYERS = asList(
            jettyDeployer(),
            pureJavaDeployer(),
            servletOnJettyDeployer(),
            fakeRestApiGatewayDeployer(),
            fakeRestApiGatewayWithWsAuthorizationDeployer(),
            fakeHttpApiGatewayV2PayloadDeployer(),
            fakeHttpApiGatewayV2PayloadWithWsAuthDeployer(),
            fakeHttpApiGatewayV1PayloadDeployer(),
            fakeHttpApiGatewayV1PayloadWithWsAuthDeployer(),
            bypassedDeployer(),
            jettyWebsocketsDeployer()
    );

    private DeployerManager() {
    }

    public static Collection<DeployerAndClient> activeDeployers() {
        return ACTIVE_DEPLOYERS.stream()
                .flatMap(deployer -> deployer.supportedClients().stream()
                        .map(clientFactory -> deployerAndClient(deployer, clientFactory)))
                .collect(toList());
    }

    public static Collection<DeployerAndClient> activeDeployersWithOnlyShittyClient() {
        return ACTIVE_DEPLOYERS.stream()
                .flatMap(deployer -> deployer.supportedClients().stream()
                        .filter(clientFactory -> clientFactory instanceof ShittyClientFactory)
                        .map(clientFactory -> deployerAndClient(deployer, clientFactory)))
                .collect(toList());
    }

    public static void cleanUpAllDeployers() {
        final List<Exception> exceptions = new LinkedList<>();
        ACTIVE_DEPLOYERS.forEach(deployer -> {
            try {
                deployer.cleanUp();
            } catch (final Exception e) {
                e.printStackTrace();
                exceptions.add(e);
            }
        });
        if (!exceptions.isEmpty()) {
            throw new RuntimeException("Exception during cleanup", exceptions.get(0));
        }
    }
}
