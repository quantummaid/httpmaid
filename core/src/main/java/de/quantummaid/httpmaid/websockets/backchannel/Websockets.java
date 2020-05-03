package de.quantummaid.httpmaid.websockets.backchannel;

import de.quantummaid.httpmaid.chains.MetaData;
import de.quantummaid.httpmaid.websockets.WebsocketSender;
import de.quantummaid.httpmaid.websockets.registry.WebsocketRegistry;
import de.quantummaid.httpmaid.websockets.registry.WebsocketRegistryEntry;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.List;

import static de.quantummaid.httpmaid.websockets.WebsocketMetaDataKeys.WEBSOCKET_REGISTRY;
import static de.quantummaid.httpmaid.websockets.WebsocketMetaDataKeys.WEBSOCKET_SENDER;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Websockets {
    private final MetaData metaData;

    public static Websockets websockets(final MetaData metaData) {
        return new Websockets(metaData);
    }

    public void sendToAllWebsocketsAuthenticatedAs(final Object authenticationInformation,
                                                   final String message) {
        throw new UnsupportedOperationException();
    }

    public void sendToAllWebsockets(final String message) {
        final WebsocketRegistry websocketRegistry = metaData.get(WEBSOCKET_REGISTRY);
        final List<WebsocketRegistryEntry> connections = websocketRegistry.connections();
        final WebsocketSender<Object> websocketSender = metaData.get(WEBSOCKET_SENDER);
        connections.forEach(entry -> {
            final Object connectionInformation = entry.connectionInformation();
            websocketSender.send(connectionInformation, message);
        });
    }

    public void sendToThisWebsocket(final String message) {
        throw new UnsupportedOperationException();
    }
}
