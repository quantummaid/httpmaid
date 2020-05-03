package de.quantummaid.httpmaid.websockets.registry;

import java.util.List;

public interface WebsocketRegistry {
    List<WebsocketRegistryEntry> connections();

    WebsocketRegistryEntry byConnectionInformation(Object connectionInformation);

    void addConnection(WebsocketRegistryEntry entry);
}
