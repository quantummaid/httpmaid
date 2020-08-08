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

package de.quantummaid.httpmaid.tests.givenwhenthen;

import de.quantummaid.httpmaid.HttpMaid;
import de.quantummaid.httpmaid.tests.givenwhenthen.builders.FirstWhenStage;
import de.quantummaid.httpmaid.tests.givenwhenthen.checkpoints.Checkpoints;
import de.quantummaid.httpmaid.tests.givenwhenthen.client.HttpClientWrapper;
import de.quantummaid.httpmaid.tests.givenwhenthen.deploy.Deployment;
import lombok.RequiredArgsConstructor;

import static de.quantummaid.httpmaid.tests.givenwhenthen.TestData.testData;
import static de.quantummaid.httpmaid.tests.givenwhenthen.checkpoints.Checkpoints.checkpoints;

@RequiredArgsConstructor
@SuppressWarnings("java:S1181")
public final class Given {
    private final TestEnvironment testEnvironment;
    private final HttpMaidSupplier httpMaidSupplier;

    public static Given given(final TestEnvironment testEnvironment,
                              final HttpMaidSupplier httpMaidSupplier) {
        return new Given(testEnvironment, httpMaidSupplier);
    }

    public FirstWhenStage when() {
        final TestData testData = testData(testEnvironment);
        final HttpMaid httpMaid;
        final Checkpoints checkpoints = checkpoints();
        testData.setCheckpoints(checkpoints);
        try {
            httpMaid = httpMaidSupplier.provide(checkpoints);
        } catch (final Throwable e) {
            testData.setInitializationException(e);
            return When.when(testData);
        }
        testEnvironment.addResourceToBeClosed(httpMaid);
        testData.setHttpMaid(httpMaid);
        final Deployment deployment = testEnvironment.getDeployer().deploy(httpMaid);
        final HttpClientWrapper clientWrapper = testEnvironment.getClientFactory().createClient(deployment);
        testData.setClientWrapper(clientWrapper);
        return When.when(testData);
    }
}
