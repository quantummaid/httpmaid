package de.quantummaid.httpmaid.tests.givenwhenthen.deploy;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import static de.quantummaid.httpmaid.tests.givenwhenthen.deploy.ApiBaseUrlProtocol.protocol;
import static de.quantummaid.httpmaid.util.Validators.validateNotNull;
import static de.quantummaid.httpmaid.util.Validators.validateNotNullNorEmpty;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ApiBaseUrl {
    public final ApiBaseUrlProtocol protocol;
    public final String hostName;
    public final Integer port;
    public final String basePath;

    public static ApiBaseUrl apiBaseUrl(final String protocol,
                                        final String hostName,
                                        final Integer port,
                                        final String basePath) {
        validateNotNull(protocol, "protocol");
        validateNotNullNorEmpty(hostName, "hostName");
        validateNotNull(port, "port");
        validateNotNullNorEmpty(basePath, "basePath");
        if (!basePath.startsWith("/")) {
            throw new IllegalArgumentException("basePath has to start with a '/'");
        }
        return new ApiBaseUrl(protocol(protocol), hostName, port, basePath);
    }

    public static ApiBaseUrl localhostHttpBaseUrl(final int port) {
        return apiBaseUrl("http", "localhost", port, "/");
    }

    public static ApiBaseUrl localhostWebsocketBaseUrl(final int port) {
        return apiBaseUrl("ws", "localhost", port, "/");
    }

    public String transportProtocol() {
        return protocol.transportProtocol();
    }

    public String toUrlString() {
        return protocol.urlProtocol() + "://" + hostName + ":" + port + basePath;
    }
}
