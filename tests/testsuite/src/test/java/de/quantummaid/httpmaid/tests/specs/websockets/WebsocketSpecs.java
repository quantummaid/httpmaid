package de.quantummaid.httpmaid.tests.specs.websockets;

import de.quantummaid.httpmaid.HttpMaid;
import de.quantummaid.httpmaid.client.websocket.Websocket;
import de.quantummaid.httpmaid.client.websocket.WebsocketClient;
import org.junit.jupiter.api.Test;

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
                })
                .build();

        final String s = httpMaid.dumpChains();
        System.out.println(s);


        final WebsocketClient websocketClient = websocketClient(httpMaid);
        final Websocket websocket = websocketClient.openWebsocket();

        websocket.send("" +
                "{" +
                "   \"message\": \"handler1\"\n" +
                "}"
        );
    }
}
