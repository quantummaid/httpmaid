package de.quantummaid.httpmaid.websockets.registry;

import java.util.List;

public interface WebsocketRegistry {
    List<ConnectionId> connections();
}
