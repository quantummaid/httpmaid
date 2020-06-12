package de.quantummaid.httpmaid.tests.givenwhenthen.deploy;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class PortDeploymentResult<T extends AutoCloseable> {
    public final Integer port;
    public final T cleanup;

    public static <T extends AutoCloseable> PortDeploymentResult<T> portDeploymentResult(
            final Integer port, final T autocloseable) {
        return new PortDeploymentResult<>(port, autocloseable);
    }
}
