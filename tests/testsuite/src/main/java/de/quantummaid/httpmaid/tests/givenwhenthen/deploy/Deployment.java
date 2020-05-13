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

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Deployment {
    private final HttpMaid httpMaid;
    private final String protocol;
    private final String httpHostname;
    private final String websocketHostname;
    private final int port;
    private final String httpBasePath;
    private final String websocketBasePath;

    public static Deployment httpsDeploymentWithBasePath(final String hostname,
                                                         final int port,
                                                         final String basePath) {
        if (!basePath.startsWith("/")) {
            throw new IllegalArgumentException("basePath has to start with a '/'");
        }
        return new Deployment(null, "https", hostname, hostname, port, basePath, basePath);
    }

    public static Deployment httpsDeploymentWithBasePath(final String httpHostname,
                                                         final String websocketHostname,
                                                         final int port,
                                                         final String httpBasePath,
                                                         final String websocketBasePath) {
        if (!httpBasePath.startsWith("/") || !websocketBasePath.startsWith("/")) {
            throw new IllegalArgumentException("basePath has to start with a '/'");
        }
        return new Deployment(null, "https", httpHostname, websocketHostname, port, httpBasePath, websocketBasePath);
    }

    public static Deployment httpDeployment(final String hostname, final int port) {
        return new Deployment(null, "http", hostname, hostname, port, "/", "/");
    }

    public static Deployment bypassedDeployment(final HttpMaid httpMaid) {
        return new Deployment(httpMaid, null, null, null, -1, null, null);
    }

    public HttpMaid httpMaid() {
        return httpMaid;
    }

    public String protocol() {
        return protocol;
    }

    public int port() {
        return port;
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
        return protocol + "://" + httpHostname + ":" + port + httpBasePath;
    }
}
