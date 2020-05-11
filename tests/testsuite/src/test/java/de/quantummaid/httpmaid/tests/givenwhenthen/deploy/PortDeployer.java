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

package de.quantummaid.httpmaid.tests.givenwhenthen.deploy;

import de.quantummaid.httpmaid.HttpMaid;

import java.util.LinkedList;
import java.util.List;

import static de.quantummaid.httpmaid.tests.givenwhenthen.deploy.FreePortPool.freePort;

public interface PortDeployer extends Deployer {

    Deployment deploy(int port, HttpMaid httpMaid);

    @Override
    default Deployment deploy(HttpMaid httpMaid) {
        cleanUp();
        final List<Exception> exceptions = new LinkedList<>();
        for (int i = 0; i < 3; ++i) {
            final int port = freePort();
            try {
                return deploy(port, httpMaid);
            } catch (final Exception e) {
                exceptions.add(e);
            }
        }
        final String message = "Failed three times to use supposedly free port.";
        System.err.println(message);
        exceptions.forEach(Throwable::printStackTrace);
        final Exception lastException = exceptions.get(exceptions.size() - 1);
        throw new IllegalStateException(message, lastException);
    }
}
