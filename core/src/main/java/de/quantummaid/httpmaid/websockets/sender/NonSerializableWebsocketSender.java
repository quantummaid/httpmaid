package de.quantummaid.httpmaid.websockets.sender;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import static de.quantummaid.httpmaid.websockets.sender.WebsocketSenderId.websocketSenderId;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class NonSerializableWebsocketSender implements WebsocketSender<NonSerializableConnectionInformation> {
    public static final WebsocketSenderId NON_SERIALIZABLE_WEBSOCKET_SENDER = websocketSenderId("NON_SERIALIZABLE_WEBSOCKET_SENDER");

    public static NonSerializableWebsocketSender nonSerializableWebsocketSender() {
        return new NonSerializableWebsocketSender();
    }

    @Override
    public void send(final NonSerializableConnectionInformation connectionInformation,
                     final String message) {
        connectionInformation.send(message);
    }

    @Override
    public WebsocketSenderId senderId() {
        return NON_SERIALIZABLE_WEBSOCKET_SENDER;
    }
}
