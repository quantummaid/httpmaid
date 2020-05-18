package de.quantummaid.httpmaid.tests.specs.websockets;

import de.quantummaid.httpmaid.tests.givenwhenthen.TestEnvironment;
import de.quantummaid.httpmaid.tests.specs.websockets.domain.BroadcastingUseCase;
import de.quantummaid.httpmaid.tests.specs.websockets.domain.MyBroadcaster;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static de.quantummaid.httpmaid.HttpMaid.anHttpMaid;
import static de.quantummaid.httpmaid.tests.givenwhenthen.TestEnvironments.ENVIRONMENTS_WITH_ALL_CAPABILITIES;

public final class BroadcastingSpecs {

    @ParameterizedTest
    @MethodSource(ENVIRONMENTS_WITH_ALL_CAPABILITIES)
    public void broadcastingTest(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMaid()
                        .post("/broadcast", BroadcastingUseCase.class)
                        .websocket("check", (request, response) -> response.setBody("websocket has been registered"))
                        .broadcast(MyBroadcaster.class, String.class, sender -> sender::sendToAll)
                        .build()
        )
                .when().aWebsocketIsConnected()
                .andWhen().aWebsocketMessageIsSent("{ \"message\": \"check\" }")
                .aWebsocketMessageHasBeenReceivedWithContent("websocket has been registered")
                .andWhen().aRequestToThePath("/broadcast").viaThePostMethod().withTheBody("{ \"message\": \"foo\" }").isIssued()
                .theStatusCodeWas(200)
                .aWebsocketMessageHasBeenReceivedWithContent("foo");
    }
}
