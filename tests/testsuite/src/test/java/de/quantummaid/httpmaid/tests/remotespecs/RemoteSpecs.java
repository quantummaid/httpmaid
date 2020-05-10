package de.quantummaid.httpmaid.tests.remotespecs;

import de.quantummaid.httpmaid.tests.givenwhenthen.TestEnvironment;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public final class RemoteSpecs {

    @ParameterizedTest
    @MethodSource(TestEnvironment.REMOTE_ENVIRONMENTS)
    public void pageNotFoundExceptionContainsContext(final TestEnvironment testEnvironment) {
        testEnvironment.given(() -> null)
                .when().aRequestToThePath("/").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theResponseBodyContains("No handler found for path '/foo' and method 'POST'");
    }
}
