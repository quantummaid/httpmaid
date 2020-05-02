package de.quantummaid.httpmaid.client.websocket;

import de.quantummaid.httpmaid.HttpMaid;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import static de.quantummaid.httpmaid.websockets.endpoint.RawWebsocketMessage.rawWebsocketMessage;
import static java.util.UUID.randomUUID;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Websocket {
    private final HttpMaid httpMaid;

    public static Websocket websocket(final HttpMaid httpMaid) {
        return new Websocket(httpMaid);
    }

    public void send(final String message) {
        final String connectionId = randomUUID().toString();
        httpMaid.handleWebsocketMessage(() -> rawWebsocketMessage(connectionId, message));
    }
}
