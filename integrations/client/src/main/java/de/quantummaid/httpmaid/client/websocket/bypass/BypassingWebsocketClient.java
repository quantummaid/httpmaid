package de.quantummaid.httpmaid.client.websocket;

import de.quantummaid.httpmaid.HttpMaid;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static de.quantummaid.httpmaid.client.websocket.BypassedWebsocket.bypassedWebsocket;
import static de.quantummaid.httpmaid.util.Validators.validateNotNull;
import static de.quantummaid.httpmaid.websockets.endpoint.RawWebsocketConnect.rawWebsocketConnect;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class BypassingWebsocketClient implements WebsocketClient {
    private final HttpMaid httpMaid;

    public static BypassingWebsocketClient bypassingWebsocketClient(final HttpMaid httpMaid) {
        validateNotNull(httpMaid, "httpMaid");
        return new BypassingWebsocketClient(httpMaid);
    }

    @Override
    public BypassedWebsocket openWebsocket(final Consumer<String> consumer,
                                           final Map<String, String> queryParameters,
                                           final Map<String, List<String>> headers) {
        httpMaid.handleRequest(
                () -> rawWebsocketConnect(consumer, queryParameters, headers),
                response -> {
                }
        );
        return bypassedWebsocket(httpMaid, consumer);
    }
}
