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

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class DeploymentBuilder {
    private String httpProtocol = "http";
    private String httpHostname = "localhost";
    private String websocketProtocol = "ws";
    private String websocketHostname = "localhost";
    private int httpPort;
    private int websocketPort;
    private String httpBasePath = "/";
    private String websocketBasePath = "/";

    public static DeploymentBuilder deploymentBuilder() {
        return new DeploymentBuilder();
    }

    public DeploymentBuilder withHttpHostname(final String httpHostname) {
        this.httpHostname = httpHostname;
        return this;
    }

    public DeploymentBuilder withWebsocketHostname(final String websocketHostname) {
        this.websocketHostname = websocketHostname;
        return this;
    }

    public DeploymentBuilder usingHttpsAndWss() {
        httpProtocol = "https";
        websocketProtocol = "wss";
        return this;
    }

    public DeploymentBuilder withHttpPort(final int port) {
        this.httpPort = port;
        return this;
    }

    public DeploymentBuilder withWebsocketPort(final int port) {
        this.websocketPort = port;
        return this;
    }

    public DeploymentBuilder withHttpBasePath(final String basePath) {
        this.httpBasePath = basePath;
        return this;
    }

    public DeploymentBuilder withWebsocketBasePath(final String basePath) {
        this.websocketBasePath = basePath;
        return this;
    }

    public Deployment build() {
        return Deployment.httpDeployment(
                httpProtocol,
                httpHostname,
                websocketProtocol,
                websocketHostname,
                httpPort,
                websocketPort,
                httpBasePath,
                websocketBasePath
        );
    }
}
