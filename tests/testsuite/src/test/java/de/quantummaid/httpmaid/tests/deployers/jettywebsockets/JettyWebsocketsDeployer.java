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

package de.quantummaid.httpmaid.tests.deployers.jettywebsockets;

import de.quantummaid.httpmaid.HttpMaid;
import de.quantummaid.httpmaid.jetty.JettyEndpoint;
import de.quantummaid.httpmaid.tests.givenwhenthen.client.ClientFactory;
import de.quantummaid.httpmaid.tests.givenwhenthen.deploy.Deployer;
import de.quantummaid.httpmaid.tests.givenwhenthen.deploy.Deployment;
import de.quantummaid.httpmaid.tests.givenwhenthen.deploy.PortDeployer;

import java.util.List;

import static de.quantummaid.httpmaid.jetty.JettyWebsocketEndpoint.jettyWebsocketEndpoint;
import static de.quantummaid.httpmaid.tests.givenwhenthen.client.real.RealHttpMaidClientFactory.theRealHttpMaidClient;
import static de.quantummaid.httpmaid.tests.givenwhenthen.client.real.RealHttpMaidClientWithConnectionReuseFactory.theRealHttpMaidClientWithConnectionReuse;
import static de.quantummaid.httpmaid.tests.givenwhenthen.client.shitty.ShittyClientFactory.theShittyTestClient;
import static de.quantummaid.httpmaid.tests.givenwhenthen.deploy.Deployment.localhostHttpAndWebsocketDeployment;
import static java.util.Arrays.asList;

public final class JettyWebsocketsDeployer implements PortDeployer {
    private JettyEndpoint current;

    private JettyWebsocketsDeployer() {
    }

    public static Deployer jettyWebsocketsDeployer() {
        return new JettyWebsocketsDeployer();
    }

    @Override
    public Deployment deploy(final int port, final HttpMaid httpMaid) {
        current = jettyWebsocketEndpoint(httpMaid, port);
        return localhostHttpAndWebsocketDeployment(port);
    }

    @Override
    public void cleanUp() {
        if (current != null) {
            try {
                current.close();
            } catch (Exception e) {
                throw new UnsupportedOperationException("Could not stop JettyEndpoint", e);
            }
        }
    }

    @Override
    public String toString() {
        return "jettywebsocket";
    }

    @Override
    public List<ClientFactory> supportedClients() {
        return asList(theShittyTestClient(), theRealHttpMaidClient(), theRealHttpMaidClientWithConnectionReuse());
    }
}
