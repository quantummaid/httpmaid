package de.quantummaid.httpmaid.client.websocket;

import de.quantummaid.httpmaid.HttpMaid;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static de.quantummaid.httpmaid.client.websocket.Websocket.websocket;
import static de.quantummaid.httpmaid.util.Validators.validateNotNull;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class WebsocketClient {
    private final HttpMaid httpMaid;

    public static WebsocketClient websocketClient(final HttpMaid httpMaid) {
        validateNotNull(httpMaid, "httpMaid");
        return new WebsocketClient(httpMaid);
    }

    public Websocket openWebsocket() {
        return openWebsocket(s -> {});
    }

    public Websocket openWebsocket(final Consumer<String> consumer) {
        return openWebsocket(consumer, Map.of());
    }

    public Websocket openWebsocket(final Consumer<String> consumer, final Map<String, List<String>> headers) {
        httpMaid.handleWebsocketConnect(consumer, headers);
        return websocket(httpMaid, consumer);
    }
}
