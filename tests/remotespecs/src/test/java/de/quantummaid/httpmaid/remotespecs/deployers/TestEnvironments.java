package de.quantummaid.httpmaid.remotespecs.deployers;

import de.quantummaid.httpmaid.tests.givenwhenthen.TestEnvironment;
import de.quantummaid.httpmaid.tests.givenwhenthen.deploy.Deployer;

import java.util.List;

import static de.quantummaid.httpmaid.tests.givenwhenthen.TestEnvironment.testEnvironment;
import static java.util.stream.Collectors.toList;

public final class TestEnvironments {
    private static final String PACKAGE = "de.quantummaid.httpmaid.remotespecs.deployers.TestEnvironments#";
    public static final String ALL_ENVIRONMENTS = PACKAGE + "allEnvironments";

    public static List<TestEnvironment> allEnvironments() {
        final List<Deployer> deployers = List.of(
                JarDeployer.jarDeployer()
        );
        return deployers.stream()
                .flatMap(deployer -> deployer.supportedClients().stream()
                        .map(client -> testEnvironment(deployer, client)))
                .collect(toList());
    }
}
