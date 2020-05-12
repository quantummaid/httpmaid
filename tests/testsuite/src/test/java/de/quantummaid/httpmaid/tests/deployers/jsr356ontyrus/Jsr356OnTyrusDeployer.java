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

package de.quantummaid.httpmaid.tests.deployers.jsr356ontyrus;

import de.quantummaid.httpmaid.HttpMaid;
import de.quantummaid.httpmaid.tests.givenwhenthen.client.ClientFactory;
import de.quantummaid.httpmaid.tests.givenwhenthen.deploy.Deployment;
import de.quantummaid.httpmaid.tests.givenwhenthen.deploy.PortDeployer;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import org.glassfish.tyrus.server.Server;

import java.util.List;

import static de.quantummaid.httpmaid.tests.givenwhenthen.client.real.RealHttpMaidClientFactory.theRealHttpMaidClient;
import static de.quantummaid.httpmaid.tests.givenwhenthen.client.real.RealHttpMaidClientWithConnectionReuseFactory.theRealHttpMaidClientWithConnectionReuse;
import static de.quantummaid.httpmaid.tests.givenwhenthen.deploy.Deployment.httpDeployment;
import static java.util.Arrays.asList;

@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Jsr356OnTyrusDeployer implements PortDeployer {
    private Server current;

    public static Jsr356OnTyrusDeployer programmaticJsr356OnTyrusDeployer() {
        return new Jsr356OnTyrusDeployer();
    }

    @Override
    public Deployment deploy(final int port, final HttpMaid httpMaid) {
        TestApplicationConfig.HTTP_MAID_HOLDER.update(httpMaid);
        current = new Server("localhost", port, "/", null, TestApplicationConfig.class);
        try {
            current.start();
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
        return httpDeployment("localhost", port);
    }

    @Override
    public void cleanUp() {
        if (current != null) {
            current.stop();
        }
    }

    @Override
    public List<ClientFactory> supportedClients() {
        return asList(theRealHttpMaidClient(), theRealHttpMaidClientWithConnectionReuse());
    }

    @Override
    public String toString() {
        return "jsr356OnTyrus";
    }
}
