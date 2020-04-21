package de.quantummaid.httpmaid.documentation.endpoints;

import de.quantummaid.httpmaid.HttpMaid;
import de.quantummaid.httpmaid.client.HttpClientRequest;
import de.quantummaid.httpmaid.client.HttpMaidClient;
import org.eclipse.jetty.server.ConnectionFactory;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.junit.jupiter.api.Test;

import javax.servlet.http.HttpServlet;

import static de.quantummaid.httpmaid.HttpMaid.anHttpMaid;
import static de.quantummaid.httpmaid.documentation.support.FreePortPool.freePort;
import static de.quantummaid.httpmaid.jetty.JettyEndpoint.jettyEndpointFor;
import static de.quantummaid.httpmaid.jettywithwebsockets.JettyEndpointWithWebSocketsSupport.jettyEndpointWithWebSocketsSupportFor;
import static de.quantummaid.httpmaid.endpoint.purejavaendpoint.PureJavaEndpoint.pureJavaEndpointFor;
import static de.quantummaid.httpmaid.servlet.ServletEndpoint.servletEndpointFor;
import static de.quantummaid.httpmaid.servletwithwebsockets.WebSocketAwareHttpMaidServlet.webSocketAwareHttpMaidServlet;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public final class EndpointsExampleTests {

    @Test
    public void pureJavaEndpoint() {
        final HttpMaid httpMaid = httpMaid();
        final int port = freePort();
        //Showcase start javaEndpoint
        pureJavaEndpointFor(httpMaid).listeningOnThePort(port);
        //Showcase end javaEndpoint
        assertForPort(port);
        httpMaid.close();
    }

    @Test
    public void jettyEndpoint() {
        final HttpMaid httpMaid = httpMaid();
        final int port = freePort();
        //Showcase start jettyEndpoint
        jettyEndpointFor(httpMaid).listeningOnThePort(port);
        //Showcase end jettyEndpoint
        assertForPort(port);
        httpMaid.close();
    }

    @Test
    public void jettyWebsocketEndpoint() {
        final HttpMaid httpMaid = httpMaid();
        final int port = freePort();
        //Showcase start jettyWebsocketsEndpoint
        jettyEndpointWithWebSocketsSupportFor(httpMaid).listeningOnThePort(port);
        //Showcase end jettyWebsocketsEndpoint
        assertForPort(port);
        httpMaid.close();
    }

    @Test
    public void servletEndpoint() {
        final HttpMaid httpMaid = httpMaid();

        //Showcase start servletSample
        final HttpServlet servlet = servletEndpointFor(httpMaid);
        //Showcase end servletSample

        final int port = freePort();
        final Server server = servletDeploy(servlet, port);
        assertForPort(port);

        try {
            server.stop();
            server.destroy();
        } catch (Exception e) {
            throw new UnsupportedOperationException("Could not stop jetty", e);
        }
    }

    @Test
    public void servletWebsocketEndpoint() {
        final HttpMaid httpMaid = httpMaid();

        //Showcase start websocketServletSample
        final HttpServlet servletWithWebsockets = webSocketAwareHttpMaidServlet(httpMaid);
        //Showcase end websocketServletSample

        final int port = freePort();
        final Server server = servletDeploy(servletWithWebsockets, port);
        assertForPort(port);

        try {
            server.stop();
            server.destroy();
        } catch (Exception e) {
            throw new UnsupportedOperationException("Could not stop jetty", e);
        }
    }

    private static HttpMaid httpMaid() {
        return anHttpMaid()
                .get("/foo", (request, response) -> response.setBody("foo"))
                .build();
    }

    private static void assertForPort(final int port) {
        final HttpMaidClient client = HttpMaidClient.aHttpMaidClientForTheHost("localhost")
                .withThePort(port)
                .viaHttp()
                .build();
        final String response = client.issue(HttpClientRequest.aGetRequestToThePath("/foo").mappedToString());
        assertThat(response, is("foo"));
    }

    private static Server servletDeploy(final HttpServlet servlet, final int port) {
        final Server server = new Server(port);

        final HttpConnectionFactory connectionFactory = extractConnectionFactory(server);
        connectionFactory.getHttpConfiguration().setFormEncodedMethods();

        final ServletHandler servletHandler = new ServletHandler();
        server.setHandler(servletHandler);
        final ServletHolder servletHolder = new ServletHolder(servlet);
        servletHandler.addServletWithMapping(servletHolder, "/*");
        try {
            server.start();
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
        return server;
    }

    private static HttpConnectionFactory extractConnectionFactory(final Server server) {
        final Connector[] connectors = server.getConnectors();
        if (connectors.length != 1) {
            throw new UnsupportedOperationException("Jetty does not behave as expected");
        }
        final Connector connector = connectors[0];
        final ConnectionFactory connectionFactory = connector.getDefaultConnectionFactory();
        if (!(connectionFactory instanceof HttpConnectionFactory)) {
            throw new UnsupportedOperationException("Jetty does not behave as expected");
        }
        return (HttpConnectionFactory) connectionFactory;
    }
}
