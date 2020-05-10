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

package de.quantummaid.httpmaid.tests.givenwhenthen.remote.warontomcat;

import de.quantummaid.httpmaid.HttpMaid;
import de.quantummaid.httpmaid.tests.givenwhenthen.client.ClientFactory;
import de.quantummaid.httpmaid.tests.givenwhenthen.deploy.Deployer;
import de.quantummaid.httpmaid.tests.givenwhenthen.deploy.Deployment;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.ContextConfig;
import org.apache.catalina.startup.Tomcat;

import java.util.List;

import static de.quantummaid.httpmaid.tests.givenwhenthen.client.real.RealHttpMaidClientFactory.theRealHttpMaidClient;
import static de.quantummaid.httpmaid.tests.givenwhenthen.client.real.RealHttpMaidClientWithConnectionReuseFactory.theRealHttpMaidClientWithConnectionReuse;
import static de.quantummaid.httpmaid.tests.givenwhenthen.deploy.Deployment.httpDeployment;
import static de.quantummaid.httpmaid.tests.givenwhenthen.remote.BaseDirectoryFinder.findProjectBaseDirectory;
import static java.util.Arrays.asList;

public final class WarOnTomcatDeployer implements Deployer {
    private static final String RELATIVE_PATH_TO_WAR = "/tests/war/target/testwar-0.9.57.war";
    private Tomcat tomcat;

    public static WarOnTomcatDeployer warOnTomcatDeployer() {
        return new WarOnTomcatDeployer();
    }

    @Override
    public Deployment deploy(final HttpMaid httpMaid) {
        final String basePath = findProjectBaseDirectory();
        final String pathToWar = basePath + RELATIVE_PATH_TO_WAR;

        return retryUntilFreePortFound(port -> {
            tomcat = new Tomcat();
            tomcat.setPort(port);
            final String basedir = "/home/marco/repositories/quantummaid/jacocotutorial/test";
            tomcat.setBaseDir(basedir);
            tomcat.getHost().setAppBase(basedir);
            tomcat.getHost().setAutoDeploy(true);
            tomcat.getHost().setDeployOnStartup(true);

            try {
                tomcat.start();
            } catch (final LifecycleException e) {
                throw new RuntimeException(e);
            }

            tomcat.addWebapp(tomcat.getHost(), "/app", pathToWar);
            tomcat.getServer().await();
            return httpDeployment("localhost", port);
        });
    }

    @Override
    public void cleanUp() {
        if (tomcat != null) {
            try {
                tomcat.stop();
            } catch (final LifecycleException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public List<ClientFactory> supportedClients() {
        return asList(theRealHttpMaidClient(), theRealHttpMaidClientWithConnectionReuse());
    }
}
