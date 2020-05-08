package de.quantummaid.httpmaid.tests.givenwhenthen.deploy.fakeawslambda.websocket;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketListener;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class MyWebsocket implements WebSocketListener {

    public static MyWebsocket myWebsocket() {
        return new MyWebsocket();
    }

    @Override
    public void onWebSocketBinary(final byte[] bytes, final int i, final int i1) {

    }

    @Override
    public void onWebSocketText(final String s) {
        System.out.println("s = " + s);
    }

    @Override
    public void onWebSocketClose(final int i, final String s) {

    }

    @Override
    public void onWebSocketConnect(final Session session) {

    }

    @Override
    public void onWebSocketError(final Throwable throwable) {

    }
}
