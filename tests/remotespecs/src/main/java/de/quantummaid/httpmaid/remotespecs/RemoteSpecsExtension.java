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
import de.quantummaid.httpmaid.tests.givenwhenthen.client.real.RealHttpMaidClientFactory;
import de.quantummaid.httpmaid.tests.givenwhenthen.deploy.Deployer;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.*;
import org.junit.jupiter.api.extension.ExtensionContext.Store;

import static de.quantummaid.httpmaid.remotespecs.AdditionalInformationCarrier.additionalInformationCarrier;
import static de.quantummaid.httpmaid.remotespecs.DummyDeployer.dummyDeployer;
import static de.quantummaid.httpmaid.tests.givenwhenthen.TestEnvironment.testEnvironment;
import static de.quantummaid.httpmaid.util.Validators.validateNotNull;
import static org.junit.jupiter.api.extension.ExtensionContext.Namespace.GLOBAL;

@Slf4j
public final class RemoteSpecsExtension implements ParameterResolver,
        BeforeEachCallback,
        AfterAllCallback,
        TestWatcher {
    private RemoteSpecsDeployer deployer;

    @Override
    public boolean supportsParameter(final ParameterContext parameterContext,
                                     final ExtensionContext extensionContext) {
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object resolveParameter(final ParameterContext parameterContext,
                                   final ExtensionContext extensionContext) {
        final Class<?> testClass = extensionContext.getRequiredTestClass();
        final RemoteSpecsDeployment deployment = getDeployment(extensionContext);
        validateNotNull(deployment, "deployment");
        final Deployer dummyDeployer = dummyDeployer(deployment.descriptorFor((Class<? extends RemoteSpecs>) testClass));
        final ClientFactory clientFactory = RealHttpMaidClientFactory.theRealHttpMaidClient();
        return testEnvironment(dummyDeployer, clientFactory);
    }

    @Override
    public void beforeEach(final ExtensionContext context) {
        if (deployer != null) {
            return;
        }
        final RemoteSpecs testInstance = (RemoteSpecs) context.getRequiredTestInstance();
        deployer = testInstance.provideDeployer();
        final RemoteSpecsDeployment existingDeployment = getDeployment(context);
        final RemoteSpecsDeployment deployment;
        if (existingDeployment != null) {
            deployment = existingDeployment;
        } else {
            deployment = deployer.deploy();
        }
        putDeployment(context, deployment);
    }

    private RemoteSpecsDeployment getDeployment(final ExtensionContext context) {
        final Store store = context.getRoot().getStore(GLOBAL);
        return store.get(deployer.getClass().getName(), RemoteSpecsDeployment.class);
    }

    private void putDeployment(final ExtensionContext context,
                               final RemoteSpecsDeployment deployment) {
        final Store store = context.getRoot().getStore(GLOBAL);
        store.put(deployer.getClass().getName(), deployment);
    }

    @Override
    public void testFailed(final ExtensionContext context, final Throwable cause) {
        final RemoteSpecs testInstance = (RemoteSpecs) context.getRequiredTestInstance();
        try {
            testInstance.additionInformationOnError().ifPresent(info -> {
                log.info("additional information for failure: {}", info);
                cause.addSuppressed(additionalInformationCarrier(info));
            });
        } catch (final Throwable e) {
            log.warn("could not determine additional information for failure", e);
        }
    }

    @Override
    public void afterAll(final ExtensionContext context) {
        // If and when we want to control the lifecycle of the 'expensive' deployment object,
        // we'll have to:
        //  - remove the implements CloseableResource in RemoteSpecsDeployment
        //  - implement some sort of reference counter here, and only call clean when the count goes to zero
    }
}
