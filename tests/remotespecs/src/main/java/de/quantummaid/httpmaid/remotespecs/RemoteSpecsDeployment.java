package de.quantummaid.httpmaid.remotespecs;

import de.quantummaid.httpmaid.tests.givenwhenthen.deploy.Deployment;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.Map;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class RemoteSpecsDeployment implements ExtensionContext.Store.CloseableResource {
    private final AutoCloseable cleanupFunction;
    private final Map<Class<? extends RemoteSpecs>, Deployment> deployments;

    public static RemoteSpecsDeployment remoteSpecsDeployment(
            final AutoCloseable cleanupFunction,
            final Map<Class<? extends RemoteSpecs>, Deployment> deployments) {
        return new RemoteSpecsDeployment(cleanupFunction, deployments);
    }

    @Override
    public void close() throws Exception {
        cleanupFunction.close();
    }

    public Deployment descriptorFor(Class<? extends RemoteSpecs> testClass) {
        return deployments.get(testClass);
    }
}
