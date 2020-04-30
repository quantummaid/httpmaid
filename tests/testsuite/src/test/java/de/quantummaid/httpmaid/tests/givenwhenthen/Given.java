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
import de.quantummaid.httpmaid.tests.givenwhenthen.client.ClientFactory;
import de.quantummaid.httpmaid.tests.givenwhenthen.client.HttpClientWrapper;
import de.quantummaid.httpmaid.tests.givenwhenthen.deploy.Deployer;
import de.quantummaid.httpmaid.tests.givenwhenthen.deploy.Deployment;
import lombok.RequiredArgsConstructor;

import java.util.function.Supplier;

@RequiredArgsConstructor
public final class Given {
    private final Supplier<HttpMaid> httpMaidSupplier;
    private final Deployer deployer;
    private final ClientFactory clientFactory;

    public static Given given(final Supplier<HttpMaid> httpMaidSupplier,
                              final Deployer deployer,
                              final ClientFactory clientFactory) {
        return new Given(httpMaidSupplier, deployer, clientFactory);
    }

    public FirstWhenStage when() {
        final HttpMaid httpMaid;
        try {
            httpMaid = httpMaidSupplier.get();
        } catch (final Throwable e) {
            return When.failureWhen(e);
        }
        final Deployment deployment = deployer.deploy(httpMaid);
        final HttpClientWrapper clientWrapper = clientFactory.createClient(deployment);
        return When.successWhen(clientWrapper);
    }
}
