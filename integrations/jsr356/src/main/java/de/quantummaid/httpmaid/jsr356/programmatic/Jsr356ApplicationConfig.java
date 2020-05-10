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

package de.quantummaid.httpmaid.jsr356.programmatic;

import de.quantummaid.httpmaid.HttpMaid;
import de.quantummaid.httpmaid.endpoint.HttpMaidProvider;

import javax.websocket.Endpoint;
import javax.websocket.server.ServerApplicationConfig;
import javax.websocket.server.ServerEndpointConfig;
import java.util.Collections;
import java.util.Set;

import static de.quantummaid.httpmaid.jsr356.programmatic.Jsr356ServerEndpointConfig.jsr356ServerEndpointConfig;

public interface Jsr356ApplicationConfig extends ServerApplicationConfig, HttpMaidProvider {

    @Override
    default Set<ServerEndpointConfig> getEndpointConfigs(final Set<Class<? extends Endpoint>> endpointClasses) {
        final HttpMaid httpMaid = provideHttpMaid();
        final ServerEndpointConfig serverEndpointConfig = jsr356ServerEndpointConfig(httpMaid);
        return Set.of(serverEndpointConfig);
    }

    @Override
    default Set<Class<?>> getAnnotatedEndpointClasses(final Set<Class<?>> scanned) {
        return Collections.emptySet();
    }
}
