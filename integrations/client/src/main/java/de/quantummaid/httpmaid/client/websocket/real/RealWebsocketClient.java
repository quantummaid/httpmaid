package de.quantummaid.httpmaid.client.websocket;

import org.eclipse.jetty.websocket.client.WebSocketClient;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public final class RealWebsocketClient implements WebsocketClient {

    @Override
    public Websocket openWebsocket(final Consumer<String> consumer,
                                   final Map<String, String> queryParameters,
                                   final Map<String, List<String>> headers) {

        final String destUri = "ws://echo.websocket.org";

        WebSocketClient client = new WebSocketClient();
        SimpleEchoSocket socket = new SimpleEchoSocket();
        try
        {
            client.start();

            URI echoUri = new URI(destUri);
            ClientUpgradeRequest request = new ClientUpgradeRequest();
            client.connect(socket, echoUri, request);
            System.out.printf("Connecting to : %s%n", echoUri);

            // wait for closed socket connection.
            socket.awaitClose(5, TimeUnit.SECONDS);
        }
        catch (Throwable t)
        {
            t.printStackTrace();
        }
        finally
        {
            try
            {
                client.stop();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        return null;
    }
}
