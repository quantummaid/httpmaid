package de.quantummaid.httpmaid.websockets.backchannel;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import static de.quantummaid.httpmaid.util.Validators.validateNotNull;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class WebsocketMessageToSend {
    private final String message;

    public static WebsocketMessageToSend websocketMessageToSend(final String message) {
        validateNotNull(message, "message");
        return new WebsocketMessageToSend(message);
    }
}
