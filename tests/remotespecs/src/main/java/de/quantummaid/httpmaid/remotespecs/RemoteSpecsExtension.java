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

package de.quantummaid.httpmaid.remotespecs;

import de.quantummaid.httpmaid.tests.givenwhenthen.client.ClientFactory;
import de.quantummaid.httpmaid.tests.givenwhenthen.deploy.Deployer;
import org.junit.jupiter.api.extension.*;
import org.junit.jupiter.api.extension.ExtensionContext.Store;

import static de.quantummaid.httpmaid.remotespecs.DummyDeployer.dummyDeployer;
import static de.quantummaid.httpmaid.tests.givenwhenthen.TestEnvironment.testEnvironment;
import static de.quantummaid.httpmaid.tests.givenwhenthen.client.shitty.ShittyClientFactory.theShittyTestClient;
import static java.util.Optional.ofNullable;
import static org.junit.jupiter.api.extension.ExtensionContext.Namespace.GLOBAL;

public final class RemoteSpecsExtension implements ParameterResolver,
        BeforeEachCallback,
        AfterAllCallback {
    private RemoteSpecsDeployer deployer;

    @Override
    public boolean supportsParameter(final ParameterContext parameterContext,
                                     final ExtensionContext extensionContext) throws ParameterResolutionException {
        return true;
    }

    @Override
    public Object resolveParameter(final ParameterContext parameterContext,
                                   final ExtensionContext ctx) throws ParameterResolutionException {

        final Class<?> testClass = ctx.getRequiredTestClass();
        final RemoteSpecsDeployment deployment = getDeployment(ctx);
        final Deployer deployer = dummyDeployer(deployment.descriptorFor((Class<? extends RemoteSpecs>) testClass));
        final ClientFactory clientFactory = theShittyTestClient();
        return testEnvironment(deployer, clientFactory);
    }

    @Override
    public void beforeEach(final ExtensionContext context) {
        if (deployer != null) {
            return;
        }
        final RemoteSpecs testInstance = (RemoteSpecs) context.getRequiredTestInstance();
        deployer = testInstance.provideDeployer();
        final RemoteSpecsDeployment existingDeployment = getDeployment(context);
        final RemoteSpecsDeployment deployment = ofNullable(existingDeployment).orElseGet(() -> deployer.deploy());
        putDeployment(context, deployment);
    }

    private RemoteSpecsDeployment getDeployment(final ExtensionContext ctx) {
        final Store store = ctx.getRoot().getStore(GLOBAL);
        final RemoteSpecsDeployment deployment = store.get(deployer.getClass().getName(), RemoteSpecsDeployment.class);
        return deployment;
    }

    private void putDeployment(final ExtensionContext ctx, final RemoteSpecsDeployment deployment) {
        final Store store = ctx.getRoot().getStore(GLOBAL);
        store.put(deployer.getClass().getName(), deployment);
    }

        @Override
    public void afterAll(final ExtensionContext context) {
        // If and when we want to control the lifecycle of the 'expensive' deployment object,
        // we'll have to:
        //  - remove the implements CloseableResource in RemoteSpecsDeployment
        //  - implement some sort of reference counter here, and only call clean when the count goes to zero
    }
}
