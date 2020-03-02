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

package de.quantummaid.httpmaid.spark;

import de.quantummaid.httpmaid.HttpMaid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import spark.Service;

import static de.quantummaid.httpmaid.closing.ClosingActions.CLOSING_ACTIONS;
import static spark.Service.ignite;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class SparkEndpoint implements AutoCloseable {
    private final HttpMaid httpMaid;

    public static PortStage sparkEndpointFor(final HttpMaid httpMaid) {
        return port -> {
            final Service spark = ignite();
            spark.port(port);
            final SparkRouteWebserviceAdapter webserviceAdapterRoute = new SparkRouteWebserviceAdapter(httpMaid);
            spark.get("/*", webserviceAdapterRoute);
            spark.post("/*", webserviceAdapterRoute);
            spark.put("/*", webserviceAdapterRoute);
            spark.delete("/*", webserviceAdapterRoute);
            spark.options("/*", webserviceAdapterRoute);
            spark.awaitInitialization();
            httpMaid.getMetaDatum(CLOSING_ACTIONS).addClosingAction(() -> {
                spark.stop();
                spark.awaitStop();
            });
            return new SparkEndpoint(httpMaid);
        };
    }

    @Override
    public void close() {
        httpMaid.close();
    }
}
