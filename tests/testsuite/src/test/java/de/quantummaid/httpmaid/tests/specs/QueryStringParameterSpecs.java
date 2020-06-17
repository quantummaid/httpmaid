package de.quantummaid.httpmaid.tests.specs;

import de.quantummaid.httpmaid.http.HttpRequestException;
import de.quantummaid.httpmaid.tests.givenwhenthen.TestEnvironment;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Map;

import static de.quantummaid.httpmaid.HttpMaid.anHttpMaid;
import static de.quantummaid.httpmaid.exceptions.ExceptionConfigurators.toMapExceptionsOfType;
import static de.quantummaid.httpmaid.tests.givenwhenthen.TestEnvironments.ALL_ENVIRONMENTS;

public class QueryStringParameterSpecs {

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void handlerCanReceiveSingleQueryStringParameter(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .get("/", (request, response) -> {
                            final Map<String, List<String>> params = request.queryParameters().asMap();
                            response.setBody(params);
                        })
                        .build()
        )
                .when().aRequestToThePath("/").viaTheGetMethod()
                .withAnEmptyBody().withQueryStringParameter("param+1 %ü", "value+1 %ü").isIssued()
                .theJsonResponseStrictlyEquals(Map.of("param+1 %ü", List.of("value+1 %ü")));
    }

    /**
     * See: https://swagger.io/docs/specification/serialization for our usage of the word 'exploded'
     */
    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void handlerCanReceiveMultiValuedQueryStringParameterInExplodedForm(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .get("/", (request, response) -> {
                            final Map<String, List<String>> parameterMap = request.queryParameters().asMap();
                            response.setBody(parameterMap);
                        })
                        .build()
        )
                .when().aRequestToThePath("/").viaTheGetMethod()
                .withAnEmptyBody()
                .withQueryStringParameter("param1", "value1")
                .withQueryStringParameter("otherparam", "othervalue")
                .withQueryStringParameter("param1", "value2")
                .isIssued()
                .theJsonResponseStrictlyEquals(
                        Map.of("param1", List.of("value1", "value2"),
                                "otherparam", List.of("othervalue")));
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void handlerCanAccessAllValuesOfMultiValueQueryParameter(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .get("/", (request, response) -> {
                            final List<String> values = request.queryParameters().allValuesFor("multiple_values");
                            response.setBody(Map.of("values", values));
                        })
                        .build()
        )
                .when().aRequestToThePath("/").viaTheGetMethod().withAnEmptyBody()
                .withQueryStringParameter("multiple_values", "value1")
                .withQueryStringParameter("multiple_values", "value2")
                .withQueryStringParameter("multiple_values", "value3")
                .isIssued()
                .theStatusCodeWas(200)
                .theResponseBodyWas("{\"values\":[\"value1\",\"value2\",\"value3\"]}");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void exceptionWhenAccessingNotExistingQueryParameter(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .get("/", (request, response) -> request.queryParameters().parameter("not_existing"))
                        .configured(toMapExceptionsOfType(HttpRequestException.class, (exception, response) -> {
                            response.setBody(exception.getMessage());
                            response.setStatus(501);
                        }))
                        .build()
        )
                .when().aRequestToThePath("/").viaTheGetMethod().withAnEmptyBody().isIssued()
                .theStatusCodeWas(501)
                .theResponseBodyWas("No query parameter with the name 'not_existing'");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void exceptionWhenHandlerAccessesMultiValueQueryParameterButExpectsSingleValue(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .get("/", (request, response) -> request.queryParameters().optionalParameter("multiple_values"))
                        .configured(toMapExceptionsOfType(HttpRequestException.class, (exception, response) -> {
                            response.setBody(exception.getMessage());
                            response.setStatus(501);
                        }))
                        .build()
        )
                .when().aRequestToThePath("/").viaTheGetMethod().withAnEmptyBody()
                .withQueryStringParameter("multiple_values", "value1")
                .withQueryStringParameter("multiple_values", "value2")
                .withQueryStringParameter("multiple_values", "value3")
                .isIssued()
                .theStatusCodeWas(501)
                .theResponseBodyWas("Expecting query string parameter 'multiple_values' to only have one value " +
                        "but got [value1, value2, value3]");
    }
}
