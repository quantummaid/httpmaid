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
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Objects;
import java.util.Optional;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Deployment {
    private final HttpMaid httpMaid;
    private final ApiBaseUrl httpBaseUrl;
    private final ApiBaseUrl webSocketBaseUrl;

    public static Deployment localhostHttpDeployment(int httpPort) {
        return httpDeployment(ApiBaseUrl.localhostHttpBaseUrl(httpPort), null);
    }

    public static Deployment localhostWebsocketDeployment(int websocketPort) {
        return httpDeployment(null, ApiBaseUrl.localhostWebsocketBaseUrl(websocketPort));
    }

    public static Deployment localhostHttpAndWebsocketDeployment(int port) {
        return httpDeployment(ApiBaseUrl.localhostHttpBaseUrl(port), ApiBaseUrl.localhostWebsocketBaseUrl(port));
    }

    public static Deployment localhostHttpAndWebsocketDeployment(int httpPort, int websocketPort) {
        return httpDeployment(ApiBaseUrl.localhostHttpBaseUrl(httpPort), ApiBaseUrl.localhostWebsocketBaseUrl(websocketPort));
    }

    public static Deployment httpDeployment(final ApiBaseUrl httpBaseUrl,
                                            final ApiBaseUrl webSocketBaseUrl) {
        return new Deployment(null, httpBaseUrl, webSocketBaseUrl);
    }

    public static Deployment bypassedDeployment(final HttpMaid httpMaid) {
        return new Deployment(httpMaid, null, null);
    }

    public Optional<HttpMaid> httpMaid() {
        return Optional.ofNullable(httpMaid);
    }

    public Optional<ApiBaseUrl> httpBaseUrl() {
        return Optional.ofNullable(httpBaseUrl);
    }

    public Optional<ApiBaseUrl> webSocketBaseUrl() {
        return Optional.ofNullable(webSocketBaseUrl);
    }
}
