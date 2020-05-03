package de.quantummaid.httpmaid.tests.specs.websockets;

import de.quantummaid.httpmaid.HttpMaid;
import de.quantummaid.httpmaid.client.websocket.Websocket;
import de.quantummaid.httpmaid.client.websocket.WebsocketClient;
import de.quantummaid.httpmaid.http.headers.ContentType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static de.quantummaid.httpmaid.HttpMaid.anHttpMaid;
import static de.quantummaid.httpmaid.client.websocket.WebsocketClient.websocketClient;

public final class WebsocketSpecs {

    @Test
    public void websocketsWork() {
        final HttpMaid httpMaid = anHttpMaid()
                .websocket("test", (request, websockets) -> System.out.println("websocket message!!!"))
                .build();

        final String s = httpMaid.dumpChains();
        System.out.println(s);

        final WebsocketClient websocketClient = websocketClient(httpMaid);
        final Websocket websocket = websocketClient.openWebsocket();
        websocket.send("fooo");
    }

    @Test
    public void websocketsCanBeMultiplexed() {
        final HttpMaid httpMaid = anHttpMaid()
                .websocket("handler1", (request, websockets) -> System.out.println("handler 1"))
                .websocket("handler2", (request, websockets) -> System.out.println("handler 2"))
                .build();

        final WebsocketClient websocketClient = websocketClient(httpMaid);
        final Websocket websocket = websocketClient.openWebsocket();

        websocket.send("" +
                "{" +
                "   \"message\": \"handler1\"\n" +
                "}"
        );

        websocket.send("" +
                "{" +
                "   \"message\": \"handler2\"\n" +
                "}"
        );
    }

    @Test
    public void websocketsCanBeAccessedArbitrarily() {
        final HttpMaid httpMaid = anHttpMaid()
                .websocket("handler1", (request, websockets) -> {
                    System.out.println("handler 1");
                    websockets.sendToAllWebsockets("foo");
                    websockets.sendToAllWebsockets("foo2");
                })
                .build();

        final WebsocketClient websocketClient = websocketClient(httpMaid);
        final Websocket websocket = websocketClient.openWebsocket(System.out::println);

        websocket.send("" +
                "{\n" +
                "   \"message\": \"handler1\"\n" +
                "}"
        );
    }

    @Test
    public void websocketsCanHaveQueryParameters() {
        final HttpMaid httpMaid = anHttpMaid()
                .websocket("handler1", (request, websockets) -> {
                    System.out.println("handler 1");
                    websockets.sendToAllWebsockets("foo");
                    websockets.sendToAllWebsockets("foo2");
                })
                .build();

        final WebsocketClient websocketClient = websocketClient(httpMaid);
        final Websocket websocket = websocketClient.openWebsocket(System.out::println);

        websocket.send("" +
                "{\n" +
                "   \"message\": \"handler1\"\n" +
                "}"
        );
    }

    @Test
    public void websocketsCanHaveHeaders() {
        final HttpMaid httpMaid = anHttpMaid()
                .websocket("handler1", (request, websockets) -> {
                    final String myHeader = request.headers().getHeader("myHeader");
                    System.out.println(myHeader);
                })
                .build();

        final WebsocketClient websocketClient = websocketClient(httpMaid);
        final Websocket websocket = websocketClient.openWebsocket(System.out::println, Map.of("myHeader", List.of("foo2")));

        websocket.send("" +
                "{\n" +
                "   \"message\": \"handler1\"\n" +
                "}"
        );
    }

    @Test
    public void websocketsCanHaveContentType() {
        final HttpMaid httpMaid = anHttpMaid()
                .websocket("handler1", (request, websockets) -> {
                    final ContentType contentType = request.contentType();
                    System.out.println(contentType.internalValueForMapping());
                })
                .build();

        final WebsocketClient websocketClient = websocketClient(httpMaid);
        final Websocket websocket = websocketClient.openWebsocket(System.out::println, Map.of("Content-Type", List.of("application/json")));

        websocket.send("" +
                "{\n" +
                "   \"message\": \"handler1\"\n" +
                "}"
        );
    }

    @Test
    public void websocketsCanBeAccessedThroughADomainInterface() {
        final HttpMaid httpMaid = anHttpMaid()
                .websocket("handler1", (request, websockets) -> {
                    System.out.println("handler 1");
                    websockets.sendToAllWebsockets("foo");
                    websockets.sendToAllWebsockets("foo2");
                })
                .build();

        final WebsocketClient websocketClient = websocketClient(httpMaid);
        final Websocket websocket = websocketClient.openWebsocket(System.out::println);

        websocket.send("" +
                "{\n" +
                "   \"message\": \"handler1\"\n" +
                "}"
        );
    }
}
