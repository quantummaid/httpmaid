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

package de.quantummaid.httpmaid.tests.deployers.jeeonundertow;

import de.quantummaid.httpmaid.HttpMaid;
import de.quantummaid.httpmaid.tests.givenwhenthen.client.ClientFactory;
import de.quantummaid.httpmaid.tests.givenwhenthen.deploy.Deployment;
import de.quantummaid.httpmaid.tests.givenwhenthen.deploy.PortDeployer;
import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.ServletInfo;
import io.undertow.websockets.jsr.WebSocketDeploymentInfo;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import javax.servlet.ServletException;
import javax.websocket.server.ServerEndpointConfig;
import java.util.List;

import static de.quantummaid.httpmaid.jsr356.Jsr356ServerEndpointConfig.jsr356ServerEndpointConfig;
import static de.quantummaid.httpmaid.tests.givenwhenthen.client.real.RealHttpMaidClientFactory.theRealHttpMaidClient;
import static de.quantummaid.httpmaid.tests.givenwhenthen.client.real.RealHttpMaidClientWithConnectionReuseFactory.theRealHttpMaidClientWithConnectionReuse;
import static de.quantummaid.httpmaid.tests.givenwhenthen.client.shitty.ShittyClientFactory.theShittyTestClient;
import static de.quantummaid.httpmaid.tests.givenwhenthen.deploy.DeploymentBuilder.deploymentBuilder;
import static java.util.Arrays.asList;

@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class JeeOnUndertowDeployer implements PortDeployer {
    private Undertow current;

    public static JeeOnUndertowDeployer jeeOnUndertowDeployer() {
        return new JeeOnUndertowDeployer();
    }

    @Override
    public Deployment deploy(final int port, final HttpMaid httpMaid) {
        ServletForUndertow.HTTP_MAID_HOLDER.update(httpMaid);
        final ServerEndpointConfig serverEndpointConfig = jsr356ServerEndpointConfig(httpMaid);
        final WebSocketDeploymentInfo webSocketDeploymentInfo = new WebSocketDeploymentInfo().addEndpoint(serverEndpointConfig);

        final ServletInfo servletInfo = Servlets.servlet("Servlet", ServletForUndertow.class)
                .addMapping("/*");

        final DeploymentInfo deploymentInfo = Servlets.deployment()
                .setDeploymentName("test")
                .setContextPath("/")
                .setClassLoader(JeeOnUndertowDeployer.class.getClassLoader())
                .addServlet(servletInfo)
                .addServletContextAttribute(WebSocketDeploymentInfo.ATTRIBUTE_NAME, webSocketDeploymentInfo);
        final DeploymentManager manager = Servlets.defaultContainer().addDeployment(deploymentInfo);
        manager.deploy();
        try {
            current = Undertow.builder()
                    .setHandler(Handlers.path().addPrefixPath(deploymentInfo.getContextPath(), manager.start()))
                    .addHttpListener(port, "localhost")
                    .build();
            current.start();
            return deploymentBuilder()
                    .withHttpPort(port)
                    .withWebsocketPort(port)
                    .build();
        } catch (final ServletException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void cleanUp() {
        if (current != null) {
            current.stop();
        }
    }

    @Override
    public List<ClientFactory> supportedClients() {
        return asList(
                theShittyTestClient(),
                theRealHttpMaidClient(),
                theRealHttpMaidClientWithConnectionReuse()
        );
    }

    @Override
    public String toString() {
        return "JeeOnUndertow";
    }
}
