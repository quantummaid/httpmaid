package de.quantummaid.httpmaid.tests.specs.websockets;

import de.quantummaid.httpmaid.http.headers.ContentType;
import de.quantummaid.httpmaid.tests.givenwhenthen.TestEnvironment;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Map;

import static de.quantummaid.httpmaid.HttpMaid.anHttpMaid;
import static de.quantummaid.httpmaid.tests.givenwhenthen.TestEnvironments.WEBSOCKET_ENVIRONMENTS;

public final class WebsocketHeaderSpecs {

    @ParameterizedTest
    @MethodSource(WEBSOCKET_ENVIRONMENTS)
    public void websocketsHandlerCanAccessHeaders(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .websocket("handler", (request, response) -> {
                            final String myHeader = request.headers().header("myHeader");
                            response.setBody(myHeader);
                        })
                        .build()
        )
                .when().aWebsocketIsConnected(Map.of(), Map.of("myHeader", List.of("foo")))
                .andWhen().aWebsocketMessageIsSent("{ \"message\": \"handler\" }")
                .aWebsocketMessageHasBeenReceivedWithContent("foo");
    }

    @ParameterizedTest
    @MethodSource(WEBSOCKET_ENVIRONMENTS)
    public void websocketHandlerCanAccessContentType(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .websocket("handler", (request, response) -> {
                            final ContentType contentType = request.contentType();
                            response.setBody(contentType.internalValueForMapping());
                        })
                        .build()
        )
                .when().aWebsocketIsConnected(Map.of(), Map.of("Content-Type", List.of("application/json")))
                .andWhen().aWebsocketMessageIsSent("{ \"message\": \"handler\" }")
                .aWebsocketMessageHasBeenReceivedWithContent("application/json");
    }

    @ParameterizedTest
    @MethodSource(WEBSOCKET_ENVIRONMENTS)
    public void websocketHandlerCanAccessRequestHeaderThatOccursMultipleTimesWithDifferentValues(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .websocket("handler", (request, response) -> {
                            final List<String> map = request.headers().allValuesFor("X-Headername");
                            response.setBody(Map.of("headers", map));
                        })
                        .build()
        )
                .when().aWebsocketIsConnected(Map.of(), Map.of("X-Headername", List.of("value1", "value2")))
                .andWhen().aWebsocketMessageIsSent("{ \"message\": \"handler\" }")
                .aWebsocketMessageHasBeenReceivedWithContent("{\"headers\":[\"value1\",\"value2\"]}");
    }
}
