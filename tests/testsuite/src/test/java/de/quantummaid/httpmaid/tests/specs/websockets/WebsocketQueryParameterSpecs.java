package de.quantummaid.httpmaid.tests.specs.websockets;

import de.quantummaid.httpmaid.tests.givenwhenthen.TestEnvironment;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Map;

import static de.quantummaid.httpmaid.HttpMaid.anHttpMaid;
import static de.quantummaid.httpmaid.tests.givenwhenthen.TestEnvironments.WEBSOCKET_ENVIRONMENTS;

public final class WebsocketQueryParameterSpecs {

    @ParameterizedTest
    @MethodSource(WEBSOCKET_ENVIRONMENTS)
    public void websocketsAccessQueryParameters(final TestEnvironment testEnvironment) {
        testEnvironment.given(anHttpMaid()
                .websocket("handler", (request, response) -> {
                    final String queryParameter = request.queryParameters().getQueryParameter("param+1 %ü");
                    response.setBody(queryParameter);
                })
                .build()
        )
                .when().aWebsocketIsConnected(Map.of("param+1 %ü", List.of("value+1 %ü")), Map.of())
                .andWhen().aWebsocketMessageIsSent("{ \"message\": \"handler\" }")
                .aWebsocketMessageHasBeenReceivedWithContent("value+1 %ü");
    }

    @ParameterizedTest
    @MethodSource(WEBSOCKET_ENVIRONMENTS)
    public void websocketsAccessMultiValuedQueryStringParameterInExplodedForm(final TestEnvironment testEnvironment) {
        testEnvironment.given(checkpoints ->
                anHttpMaid()
                        .websocket("handler", (request, response) -> {
                            final Map<String, List<String>> parameterMap = request.queryParameters().asMap();
                            response.setBody(parameterMap);
                        })
                        .build()
        )
                .when().aWebsocketIsConnected(Map.of(
                "param1", List.of("value1", "value2"),
                "otherparam", List.of("othervalue"
                )
        ), Map.of())
                .andWhen().aWebsocketMessageIsSent("{ \"message\": \"handler\" }")
                .aWebsocketMessageHasBeenReceivedWithContent("{\"otherparam\":[\"othervalue\"],\"param1\":[\"value1\",\"value2\"]}");
    }
}
