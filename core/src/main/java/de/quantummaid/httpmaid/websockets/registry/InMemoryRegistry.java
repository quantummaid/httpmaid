package de.quantummaid.httpmaid.websockets.registry;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class InMemoryRegistry implements WebsocketRegistry {
    private final List<WebsocketRegistryEntry> entries;

    public static WebsocketRegistry inMemoryRegistry() {
        return new InMemoryRegistry(new ArrayList<>());
    }

    @Override
    public List<WebsocketRegistryEntry> connections() {
        return entries;
    }

    @Override
    public WebsocketRegistryEntry byConnectionInformation(final Object connectionInformation) {
        return entries.stream()
                .filter(entry -> entry.connectionInformation().equals(connectionInformation))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(format("No websocket registered by '%s'", connectionInformation)));
    }

    @Override
    public void addConnection(final WebsocketRegistryEntry entry) {
        this.entries.add(entry);
    }
}
