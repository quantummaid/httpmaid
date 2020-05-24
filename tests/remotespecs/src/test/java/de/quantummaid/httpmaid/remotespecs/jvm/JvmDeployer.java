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

package de.quantummaid.httpmaid.remotespecs.jvm;

import de.quantummaid.httpmaid.HttpMaid;
import de.quantummaid.httpmaid.tests.givenwhenthen.client.ClientFactory;
import de.quantummaid.httpmaid.tests.givenwhenthen.deploy.Deployment;
import de.quantummaid.httpmaid.tests.givenwhenthen.deploy.PortDeployer;
import de.quantummaid.httpmaid.undertow.UndertowEndpoint;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.List;

import static de.quantummaid.httpmaid.remotespecsinstance.HttpMaidFactory.httpMaid;
import static de.quantummaid.httpmaid.tests.givenwhenthen.deploy.DeploymentBuilder.deploymentBuilder;
import static de.quantummaid.httpmaid.undertow.UndertowEndpoint.startUndertowEndpoint;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class JvmDeployer implements PortDeployer {
    private UndertowEndpoint endpoint;

    public static JvmDeployer jvmDeployer() {
        return new JvmDeployer();
    }

    @Override
    public Deployment deploy(final int port, final HttpMaid httpMaid) {
        final HttpMaid realHttpMaid = httpMaid();
        endpoint = startUndertowEndpoint(realHttpMaid, port);
        return deploymentBuilder()
                .withHttpPort(port)
                .withWebsocketPort(port)
                .build();
    }

    @Override
    public void cleanUp() {
        if (endpoint != null) {
            endpoint.close();
        }
    }

    @Override
    public List<ClientFactory> supportedClients() {
        throw new UnsupportedOperationException();
    }
}