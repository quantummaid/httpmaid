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

package de.quantummaid.httpmaid.jsr356.annotated;

import de.quantummaid.httpmaid.HttpMaid;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import javax.websocket.Endpoint;
import javax.websocket.server.ServerApplicationConfig;
import javax.websocket.server.ServerEndpointConfig;
import java.util.Collections;
import java.util.Set;

import static de.quantummaid.httpmaid.jsr356.annotated.Jsr356ServerEndpointConfig.jsr356ServerEndpointConfig;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public abstract class Jsr356ApplicationConfig implements ServerApplicationConfig {
    private final HttpMaid httpMaid;

    @Override
    public Set<ServerEndpointConfig> getEndpointConfigs(final Set<Class<? extends Endpoint>> endpointClasses) {
        return Set.of(jsr356ServerEndpointConfig(httpMaid));
    }

    @Override
    public Set<Class<?>> getAnnotatedEndpointClasses(final Set<Class<?>> scanned) {
        return Collections.emptySet();
    }
}
