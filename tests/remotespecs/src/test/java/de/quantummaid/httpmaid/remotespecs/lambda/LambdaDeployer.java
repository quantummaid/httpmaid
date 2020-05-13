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

package de.quantummaid.httpmaid.remotespecs.lambda;

import de.quantummaid.httpmaid.HttpMaid;
import de.quantummaid.httpmaid.tests.givenwhenthen.client.ClientFactory;
import de.quantummaid.httpmaid.tests.givenwhenthen.deploy.Deployer;
import de.quantummaid.httpmaid.tests.givenwhenthen.deploy.Deployment;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.List;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class LambdaDeployer implements Deployer {
    private static final String DOMAIN = "execute-api.eu-west-1.amazonaws.com";
    private static final String HTTP_API = "4zwrbier78";
    private static final String WEBSOCKET_API = "5l2betejm3";
    private static final String HTTP_STAGE = "foo";
    private static final String WEBSOCKET_STAGE = "foo";
    private static final int PORT = 443;

    public static LambdaDeployer lambdaDeployer() {
        return new LambdaDeployer();
    }

    @Override
    public Deployment deploy(final HttpMaid httpMaid) {
        final String httpHost = String.format("%s.%s", HTTP_API, DOMAIN);
        final String httpBasePath = String.format("/%s", HTTP_STAGE);
        final String websocketHost = String.format("%s.%s", WEBSOCKET_API, DOMAIN);
        final String websocketBasePath = String.format("/%s", WEBSOCKET_STAGE);
        return Deployment.httpsDeploymentWithBasePath(httpHost, websocketHost, PORT, httpBasePath, websocketBasePath);
    }

    @Override
    public void cleanUp() {
    }

    @Override
    public List<ClientFactory> supportedClients() {
        throw new UnsupportedOperationException();
    }
}
