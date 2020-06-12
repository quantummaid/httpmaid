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

package de.quantummaid.httpmaid.remotespecstomcat;

import de.quantummaid.httpmaid.HttpMaid;
import de.quantummaid.httpmaid.remotespecs.BaseDirectoryFinder;
import de.quantummaid.httpmaid.remotespecs.RemoteSpecsDeployer;
import de.quantummaid.httpmaid.remotespecs.RemoteSpecsDeployment;
import de.quantummaid.httpmaid.tests.givenwhenthen.client.ClientFactory;
import de.quantummaid.httpmaid.tests.givenwhenthen.deploy.Deployment;
import de.quantummaid.httpmaid.tests.givenwhenthen.deploy.PortDeployer;
import de.quantummaid.httpmaid.tests.givenwhenthen.deploy.PortDeploymentResult;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static de.quantummaid.httpmaid.remotespecs.RemoteSpecsDeployment.remoteSpecsDeployment;
import static de.quantummaid.httpmaid.tests.givenwhenthen.deploy.Deployment.localhostHttpAndWebsocketDeployment;

@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class TomcatDeployer implements RemoteSpecsDeployer, PortDeployer {
    private static final String RELATIVE_PATH_TO_WAR = "/tests/war/target/remotespecs.war";
    private Tomcat tomcat;

    public static TomcatDeployer tomcatDeployer() {
        return new TomcatDeployer();
    }

    @Override
    public RemoteSpecsDeployment deploy() {
        final PortDeploymentResult<AutoCloseable> result =
                PortDeployer.tryToDeploy(port -> {
                    final Tomcat tomcat = deployTomcatOnPort(port);
                    return () -> tomcat.stop();
                });

        final Deployment tomcatDeployment = localhostHttpAndWebsocketDeployment(result.port);
        final RemoteSpecsDeployment remoteSpecsDeployment =
                remoteSpecsDeployment(result.cleanup, Map.of(TomcatRemoteSpecs.class, tomcatDeployment));
        return remoteSpecsDeployment;
    }

    @Override
    public Deployment deploy(final int port, final HttpMaid httpMaid) {
        deployTomcatOnPort(port);
        return localhostHttpAndWebsocketDeployment(port);
    }

    private Tomcat deployTomcatOnPort(int port) {
        tomcat = new Tomcat();
        tomcat.setPort(port);

        final String basedir = createTempDirectory();
        tomcat.setBaseDir(basedir);
        tomcat.getHost().setAppBase(basedir);
        tomcat.getHost().setAutoDeploy(true);
        tomcat.getHost().setDeployOnStartup(true);

        try {
            tomcat.start();
        } catch (final LifecycleException e) {
            throw new RuntimeException(e);
        }
        final String basePath = BaseDirectoryFinder.findProjectBaseDirectory();
        final String pathToWar = basePath + RELATIVE_PATH_TO_WAR;
        tomcat.addWebapp(tomcat.getHost(), "/", pathToWar);
        return tomcat;
    }

    private String createTempDirectory() {
        try {
            final Path basedirPath = Files.createTempDirectory("httpmaid-tomcatdeployer");
            return basedirPath.toFile().getAbsolutePath();
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
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
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return "tomcat";
    }
}
