package de.quantummaid.httpmaid.tests.givenwhenthen.deploy.fakeawslambda.websocket;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

import static de.quantummaid.httpmaid.tests.givenwhenthen.deploy.fakeawslambda.websocket.MyAdvancedEchoCreator.myAdvancedEchoCreator;

@ToString
@EqualsAndHashCode(callSuper = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class TestServlet extends WebSocketServlet {

    public static TestServlet testServlet() {
        return new TestServlet();
    }

    @Override
    public void configure(final WebSocketServletFactory webSocketServletFactory) {
        webSocketServletFactory.setCreator(myAdvancedEchoCreator());
    }
}
