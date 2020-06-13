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

import static java.lang.String.format;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Deployment {
    private final HttpMaid httpMaid;
    private final String httpProtocol;
    private final String httpHostname;
    private final String websocketProtocol;
    private final String websocketHostname;
    private final int httpPort;
    private final int websocketPort;
    private final String httpBasePath;
    private final String websocketBasePath;

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
        if (httpBaseUrl.basePath != null && !httpBaseUrl.basePath.startsWith("/")) {
            throw new IllegalArgumentException("httpBasePath has to start with a '/'");
        }
        if (webSocketBaseUrl.basePath != null && !webSocketBaseUrl.basePath.startsWith("/")) {
            throw new IllegalArgumentException("websocketBasePath has to start with a '/'");
        }
        return new Deployment(
                null,
                httpBaseUrl.protocol,
                httpBaseUrl.hostName,
                webSocketBaseUrl.protocol,
                webSocketBaseUrl.hostName,
                httpBaseUrl.port,
                webSocketBaseUrl.port,
                httpBaseUrl.basePath,
                webSocketBaseUrl.basePath
        );
    }

    public static Deployment bypassedDeployment(final HttpMaid httpMaid) {
        return new Deployment(httpMaid, null, null, null, null, -1, -1, null, null);
    }

    public HttpMaid httpMaid() {
        return httpMaid;
    }

    public String protocol() {
        return httpProtocol;
    }

    public int httpPort() {
        return httpPort;
    }

    public int websocketPort() {
        return websocketPort;
    }

    public String httpHostname() {
        return httpHostname;
    }

    public String websocketHostname() {
        return websocketHostname;
    }

    public String httpBasePath() {
        return httpBasePath;
    }

    public String websocketBasePath() {
        return websocketBasePath;
    }

    public String baseUrl() {
        return httpProtocol + "://" + httpHostname + ":" + httpPort + httpBasePath;
    }

    public String websocketUri() {
        return format("%s://%s:%d%s", websocketProtocol, websocketHostname, websocketPort, websocketBasePath);
    }
}
