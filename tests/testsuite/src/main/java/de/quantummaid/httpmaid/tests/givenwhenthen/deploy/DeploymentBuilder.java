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
