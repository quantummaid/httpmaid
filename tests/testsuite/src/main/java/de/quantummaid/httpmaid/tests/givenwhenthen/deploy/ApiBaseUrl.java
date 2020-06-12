package de.quantummaid.httpmaid.tests.givenwhenthen.deploy;

import lombok.Data;

@Data
public class ApiBaseUrl {
    public final String protocol;
    public final String hostName;
    public final Integer port;
    public final String basePath;

    public static ApiBaseUrl localhostHttpBaseUrl(final int port) {
        return new ApiBaseUrl("http", "localhost", port, "/");
    }

    public static ApiBaseUrl localhostWebsocketBaseUrl(final int port) {
        return new ApiBaseUrl("ws", "localhost", port, "/");
    }
}
