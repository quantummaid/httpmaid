package de.quantummaid.httpmaid.tests.givenwhenthen.deploy.fakeawslambda.websocket;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.eclipse.jetty.websocket.servlet.WebSocketCreator;

import static de.quantummaid.httpmaid.tests.givenwhenthen.deploy.fakeawslambda.websocket.MyWebsocket.myWebsocket;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class FakeLambdaWebsocket implements WebSocketCreator {

    public static FakeLambdaWebsocket myAdvancedEchoCreator() {
        return new FakeLambdaWebsocket();
    }

    @Override
    public Object createWebSocket(final ServletUpgradeRequest request,
                                  final ServletUpgradeResponse response) {
        return myWebsocket();
    }
}