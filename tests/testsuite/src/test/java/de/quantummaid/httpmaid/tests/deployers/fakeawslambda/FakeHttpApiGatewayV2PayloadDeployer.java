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
import de.quantummaid.httpmaid.tests.deployers.fakeawslambda.httpapi.FakeHttpLambda;
import de.quantummaid.httpmaid.tests.deployers.fakeawslambda.websocket.FakeWebsocketLambda;
import de.quantummaid.httpmaid.tests.givenwhenthen.client.ClientFactory;
import de.quantummaid.httpmaid.tests.givenwhenthen.deploy.Deployer;
import de.quantummaid.httpmaid.tests.givenwhenthen.deploy.Deployment;
import de.quantummaid.httpmaid.tests.givenwhenthen.deploy.PortDeployer;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static de.quantummaid.httpmaid.awslambda.AwsLambdaEndpoint.awsLambdaEndpointFor;
import static de.quantummaid.httpmaid.awslambda.AwsWebsocketLambdaEndpoint.awsWebsocketLambdaEndpointFor;
import static de.quantummaid.httpmaid.tests.deployers.fakeawslambda.httpapi.FakeHttpLambda.fakeHttpLambda;
import static de.quantummaid.httpmaid.tests.deployers.fakeawslambda.websocket.FakeWebsocketLambda.fakeWebsocketLambda;
import static de.quantummaid.httpmaid.tests.givenwhenthen.client.real.RealHttpMaidClientFactory.theRealHttpMaidClient;
import static de.quantummaid.httpmaid.tests.givenwhenthen.client.real.RealHttpMaidClientWithConnectionReuseFactory.theRealHttpMaidClientWithConnectionReuse;
import static de.quantummaid.httpmaid.tests.givenwhenthen.deploy.Deployment.localhostHttpAndWebsocketDeployment;
import static de.quantummaid.httpmaid.tests.givenwhenthen.deploy.FreePortPool.freePort;
import static java.util.Arrays.asList;

@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class FakeHttpApiGatewayV2PayloadDeployer implements PortDeployer {
    private FakeHttpLambda currentHttpGateway;
    private FakeWebsocketLambda currentWebsocketGateway;

    public static Deployer fakeHttpApiGatewayV2PayloadDeployer() {
        return new FakeHttpApiGatewayV2PayloadDeployer();
    }

    @Override
    public Deployment deploy(final int port, final HttpMaid httpMaid) {
        final AwsLambdaEndpoint awsLambdaEndpoint = awsLambdaEndpointFor(httpMaid);
        final AwsWebsocketLambdaEndpoint awsWebsocketLambdaEndpoint = awsWebsocketLambdaEndpointFor(httpMaid);
        currentHttpGateway = fakeHttpLambda(awsLambdaEndpoint, port);
        final int websocketsPort = freePort();
        currentWebsocketGateway = fakeWebsocketLambda(awsWebsocketLambdaEndpoint, websocketsPort);
        return localhostHttpAndWebsocketDeployment(port, websocketsPort);
    }

    @Override
    public void cleanUp() {
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
                theRealHttpMaidClient(),
                theRealHttpMaidClientWithConnectionReuse()
        );
    }
}
