package de.quantummaid.httpmaid.websockets.broadcast;

import de.quantummaid.httpmaid.websockets.registry.WebsocketRegistry;
import de.quantummaid.httpmaid.websockets.registry.WebsocketRegistryEntry;
import de.quantummaid.httpmaid.websockets.sender.WebsocketSender;
import de.quantummaid.httpmaid.websockets.sender.WebsocketSenderId;
import de.quantummaid.httpmaid.websockets.sender.WebsocketSenders;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.List;

import static de.quantummaid.httpmaid.websockets.WebsocketMetaDataKeys.WEBSOCKET_REGISTRY;
import static de.quantummaid.httpmaid.websockets.broadcast.RecipientDeterminator.all;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class SerializingSender<T> {
    private final WebsocketRegistry websocketRegistry;
    private final WebsocketSenders websocketSenders;

    public static <T> SerializingSender<T> serializingSender(final WebsocketRegistry websocketRegistry,
                                                             final WebsocketSenders websocketSenders) {
        return new SerializingSender<>(websocketRegistry, websocketSenders);
    }

    public void sendToAll(final T message) {
        sendToAllThat(message, all());
    }

    public void sendToAllAuthenticatedAs(final T message, final Object authenticationInformation) {
        throw new UnsupportedOperationException();
    }

    public void sendToAllThat(final T message, final RecipientDeterminator recipientDeterminator) {
        final List<WebsocketRegistryEntry> connections = websocketRegistry.connections();
        connections.forEach(connection -> {
            final WebsocketSenderId websocketSenderId = connection.senderId();
            final WebsocketSender<Object> sender = websocketSenders.senderById(websocketSenderId);
            final Object connectionInformation = connection.connectionInformation();
            sender.send(connectionInformation, (String) message);
        });
    }
}
