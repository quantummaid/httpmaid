package de.quantummaid.httpmaid.client.websocket;

import de.quantummaid.httpmaid.HttpMaid;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.function.Consumer;

import static de.quantummaid.httpmaid.websockets.endpoint.RawWebsocketMessage.rawWebsocketMessage;
import static java.util.UUID.randomUUID;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Websocket {
    private final HttpMaid httpMaid;
    private final Consumer<String> consumer;
    private final String connectionId;

    public static Websocket websocket(final HttpMaid httpMaid,
                                      final Consumer<String> consumer) {
        final String connectionId = randomUUID().toString();
        return new Websocket(httpMaid, consumer, connectionId);
    }

    public void send(final String message) {
        httpMaid.handleWebsocketMessage(() -> rawWebsocketMessage(consumer, message, (connectionInformation, messageToSend) -> {
            if (!(connectionInformation instanceof Consumer)) {
                throw new UnsupportedOperationException(); // TODO
            }
            ((Consumer) connectionInformation).accept(messageToSend);
        }));
    }
}
