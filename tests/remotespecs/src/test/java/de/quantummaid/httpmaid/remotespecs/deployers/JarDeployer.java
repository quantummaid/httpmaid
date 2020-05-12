package de.quantummaid.httpmaid.remotespecs.deployers;

import de.quantummaid.httpmaid.HttpMaid;
import de.quantummaid.httpmaid.remotespecs.BaseDirectoryFinder;
import de.quantummaid.httpmaid.tests.givenwhenthen.Poller;
import de.quantummaid.httpmaid.tests.givenwhenthen.client.ClientFactory;
import de.quantummaid.httpmaid.tests.givenwhenthen.deploy.Deployment;
import de.quantummaid.httpmaid.tests.givenwhenthen.deploy.PortDeployer;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import static de.quantummaid.httpmaid.tests.givenwhenthen.client.real.RealHttpMaidClientFactory.theRealHttpMaidClient;
import static de.quantummaid.httpmaid.tests.givenwhenthen.client.real.RealHttpMaidClientWithConnectionReuseFactory.theRealHttpMaidClientWithConnectionReuse;
import static de.quantummaid.httpmaid.tests.givenwhenthen.deploy.Deployment.httpDeployment;
import static java.lang.String.format;
import static java.util.Arrays.asList;

@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class JarDeployer implements PortDeployer {
    private static final String RELATIVE_PATH_TO_JAR = "/tests/jar/target/remotespecs.jar";
    private static final String READY_MESSAGE = "TestJar ready to be tested.";

    private Process process;

    public static JarDeployer jarDeployer() {
        return new JarDeployer();
    }

    @Override
    public Deployment deploy(final int port,
                             final HttpMaid httpMaid) {
        final String projectBaseDirectory = BaseDirectoryFinder.findProjectBaseDirectory();
        final String jarPath = projectBaseDirectory + RELATIVE_PATH_TO_JAR;
        final String command = format("java -jar %s %d", jarPath, port);
        try {
            process = Runtime.getRuntime().exec(command);
            waitForEndpointToBecomeAvailable();
            return httpDeployment("localhost", port);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void waitForEndpointToBecomeAvailable() {
        process.getInputStream();
        final BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        final StringBuilder stringBuilder = new StringBuilder();
        final boolean ready = Poller.pollWithTimeout(() -> {
            try {
                while (reader.ready()) {
                    final String line = reader.readLine();
                    stringBuilder.append(line);
                    if (READY_MESSAGE.equals(line)) {
                        return true;
                    }
                }
                return false;
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        });
        if (!ready) {
            process.destroy();
            throw new RuntimeException(format(
                    "JAR endpoint did not become available in time. Output:%n%s",
                    stringBuilder.toString())
            );
        }
    }

    @Override
    public void cleanUp() {
        if (process != null) {
            process.destroy();
        }
    }

    @Override
    public List<ClientFactory> supportedClients() {
        return asList(
                //theShittyTestClient(), // TODO
                theRealHttpMaidClient(),
                theRealHttpMaidClientWithConnectionReuse()
        );
    }

    @Override
    public String toString() {
        return "jar";
    }
}
