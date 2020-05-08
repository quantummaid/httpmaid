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
public final class BypassedWebsocket {
    private final HttpMaid httpMaid;
    private final Consumer<String> consumer;
    private final String connectionId;

    public static BypassedWebsocket bypassedWebsocket(final HttpMaid httpMaid,
                                                      final Consumer<String> consumer) {
        final String connectionId = randomUUID().toString();
        return new BypassedWebsocket(httpMaid, consumer, connectionId);
    }

    public void send(final String message) {
        httpMaid.handleRequest(
                () -> rawWebsocketMessage(consumer, message),
                response -> response.optionalStringBody()
                        .ifPresent(consumer)
        );
    }
}
