package de.quantummaid.httpmaid.tests.specs;

import de.quantummaid.httpmaid.tests.givenwhenthen.TestEnvironment;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Map;

import static de.quantummaid.httpmaid.HttpMaid.anHttpMaid;
import static de.quantummaid.httpmaid.tests.givenwhenthen.TestEnvironments.ALL_ENVIRONMENTS;

public class QueryStringParameterSpecs {
    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void handlerCanReceiveSingleQueryParameter(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .get("/", (request, response) -> {
                            final Map<String, String> params = request.queryParameters().asStringMap();
                            response.setBody(params);
                        })
                        .build()
        )
                .when().aRequestToThePath("/").viaTheGetMethod()
                .withAnEmptyBody().withQueryStringParameter("param1", "value1").isIssued()
                .theJsonResponseStrictlyEquals(Map.of("param1", "value1"));
    }
}
