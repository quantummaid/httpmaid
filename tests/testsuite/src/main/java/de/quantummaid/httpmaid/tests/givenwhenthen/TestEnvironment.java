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
import de.quantummaid.httpmaid.tests.givenwhenthen.client.ClientFactory;
import de.quantummaid.httpmaid.tests.givenwhenthen.deploy.Deployer;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static de.quantummaid.httpmaid.util.Validators.validateNotNull;

@Slf4j
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class TestEnvironment implements AutoCloseable {
    @Getter
    private final Deployer deployer;
    @Getter
    private final ClientFactory clientFactory;
    private final List<AutoCloseable> resources = new ArrayList<>();

    public static TestEnvironment testEnvironment(final Deployer deployer,
                                                  final ClientFactory clientFactory) {
        validateNotNull(deployer, "deployer");
        validateNotNull(clientFactory, "clientFactory");
        return new TestEnvironment(deployer, clientFactory);
    }

    public Given given(final HttpMaidSupplier httpMaidSupplier) {
        deployer.cleanUp();
        return Given.given(this, httpMaidSupplier);
    }

    public Given given(final Supplier<HttpMaid> httpMaidSupplier) {
        return given(checkpoints -> httpMaidSupplier.get());
    }

    public Given given(final HttpMaid httpMaid) {
        return given(() -> httpMaid);
    }

    public Given givenTheStaticallyDeployedTestInstance() {
        return given((HttpMaid) null);
    }

    public void addResourceToBeClosed(final AutoCloseable autoCloseable) {
        resources.add(autoCloseable);
    }

    @Override
    public void close() {
        closeResource(deployer::cleanUp);
        resources.forEach(this::closeResource);
    }

    private void closeResource(final AutoCloseable autoCloseable) {
        try {
            autoCloseable.close();
        } catch (final Exception e) {
            log.warn("Error closing resource", e);
        }
    }
}
