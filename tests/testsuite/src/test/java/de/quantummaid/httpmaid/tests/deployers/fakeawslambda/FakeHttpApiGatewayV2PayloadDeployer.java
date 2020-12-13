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

package de.quantummaid.httpmaid.tests.deployers.fakeawslambda;

import de.quantummaid.httpmaid.HttpMaid;
import de.quantummaid.httpmaid.awslambda.AwsLambdaEndpoint;
import de.quantummaid.httpmaid.awslambda.AwsWebsocketLambdaEndpoint;
import de.quantummaid.httpmaid.awslambda.apigateway.ApiGatewayClientFactory;
import de.quantummaid.httpmaid.tests.deployers.fakeawslambda.apigateway.FakeHttpV2PayloadApiGateway;
import de.quantummaid.httpmaid.tests.deployers.fakeawslambda.websocket.ApiWebsockets;
import de.quantummaid.httpmaid.tests.deployers.fakeawslambda.websocket.FakeWebsocketLambda;
import de.quantummaid.httpmaid.tests.givenwhenthen.client.ClientFactory;
import de.quantummaid.httpmaid.tests.givenwhenthen.deploy.Deployer;
import de.quantummaid.httpmaid.tests.givenwhenthen.deploy.Deployment;
import de.quantummaid.httpmaid.tests.givenwhenthen.deploy.FreePortPool;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static de.quantummaid.httpmaid.awslambda.AwsLambdaEndpoint.awsLambdaEndpointFor;
import static de.quantummaid.httpmaid.awslambda.AwsWebsocketLambdaEndpoint.awsWebsocketLambdaEndpointFor;
import static de.quantummaid.httpmaid.tests.deployers.fakeawslambda.FakeApiGatewayClientFactory.fakeApiGatewayClientFactory;
import static de.quantummaid.httpmaid.tests.deployers.fakeawslambda.FakeApiGatewayManagementServer.start;
import static de.quantummaid.httpmaid.tests.deployers.fakeawslambda.apigateway.FakeHttpV2PayloadApiGateway.fakeHttpV2PayloadApiGateway;
import static de.quantummaid.httpmaid.tests.deployers.fakeawslambda.websocket.ApiWebsockets.apiWebsockets;
import static de.quantummaid.httpmaid.tests.deployers.fakeawslambda.websocket.FakeWebsocketLambda.fakeWebsocketLambda;
import static de.quantummaid.httpmaid.tests.givenwhenthen.client.real.RealHttpMaidClientFactory.theRealHttpMaidClient;
import static de.quantummaid.httpmaid.tests.givenwhenthen.client.real.RealHttpMaidClientWithConnectionReuseFactory.theRealHttpMaidClientWithConnectionReuse;
import static de.quantummaid.httpmaid.tests.givenwhenthen.client.shitty.ShittyClientFactory.theShittyTestClient;
import static de.quantummaid.httpmaid.tests.givenwhenthen.deploy.Deployment.localhostHttpAndWebsocketDeployment;
import static de.quantummaid.httpmaid.tests.givenwhenthen.deploy.FreePortPool.freePort;
import static java.util.Arrays.asList;

@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class FakeHttpApiGatewayV2PayloadDeployer implements Deployer {
    private FakeApiGatewayManagementServer apiGatewayManagementServer;
    private FakeHttpV2PayloadApiGateway currentHttpGateway;
    private FakeWebsocketLambda currentWebsocketGateway;

    public static Deployer fakeHttpApiGatewayV2PayloadDeployer() {
        return new FakeHttpApiGatewayV2PayloadDeployer();
    }

    @Override
    public Deployment deploy(final HttpMaid httpMaid) {
        CredentialsFixer.fixCredentials();

        final ApiWebsockets apiWebsockets = apiWebsockets();

        final int apiGatewayManagementServerPort = freePort();
        apiGatewayManagementServer = start(apiGatewayManagementServerPort, apiWebsockets);

        final AwsLambdaEndpoint awsLambdaEndpoint = awsLambdaEndpointFor(httpMaid);
        final ApiGatewayClientFactory apiGatewayClientFactory = fakeApiGatewayClientFactory(apiGatewayManagementServerPort);
        final AwsWebsocketLambdaEndpoint awsWebsocketLambdaEndpoint = awsWebsocketLambdaEndpointFor(
                httpMaid,
                "not-an-actual-region",
                apiGatewayClientFactory
        );

        final int httpPort = FreePortPool.freePort();
        currentHttpGateway = fakeHttpV2PayloadApiGateway(awsLambdaEndpoint, httpPort);

        final int websocketsPort = freePort();
        currentWebsocketGateway = fakeWebsocketLambda(awsWebsocketLambdaEndpoint, null, websocketsPort, apiWebsockets);

        return localhostHttpAndWebsocketDeployment(httpPort, websocketsPort);
    }

    @Override
    public void cleanUp() {
        if (apiGatewayManagementServer != null) {
            try {
                apiGatewayManagementServer.close();
            } catch (Exception e) {
                throw new UnsupportedOperationException("Could not stop server", e);
            }
        }
        if (currentHttpGateway != null) {
            try {
                currentHttpGateway.close();
            } catch (Exception e) {
                throw new UnsupportedOperationException("Could not stop server", e);
            }
        }
        if (currentWebsocketGateway != null) {
            try {
                currentWebsocketGateway.close();
            } catch (Exception e) {
                throw new UnsupportedOperationException("Could not stop server", e);
            }
        }
    }

    @Override
    public String toString() {
        return "apiGatewayV2Payload";
    }

    @Override
    public List<ClientFactory> supportedClients() {
        return asList(
                theShittyTestClient(),
                theRealHttpMaidClient(),
                theRealHttpMaidClientWithConnectionReuse()
        );
    }
}
