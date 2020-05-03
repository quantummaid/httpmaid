package de.quantummaid.httpmaid.websockets.processors;

import de.quantummaid.httpmaid.chains.MetaData;
import de.quantummaid.httpmaid.chains.Processor;
import de.quantummaid.httpmaid.websockets.registry.WebsocketRegistry;
import de.quantummaid.httpmaid.websockets.registry.WebsocketRegistryEntry;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import static de.quantummaid.httpmaid.websockets.WebsocketMetaDataKeys.WEBSOCKET_REGISTRY;
import static de.quantummaid.httpmaid.websockets.registry.WebsocketRegistryEntry.loadFromMetaData;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class PutWebsocketInRegistryProcessor implements Processor {

    public static Processor putWebsocketInRegistryProcessor() {
        return new PutWebsocketInRegistryProcessor();
    }

    @Override
    public void apply(final MetaData metaData) {
        final WebsocketRegistryEntry entry = loadFromMetaData(metaData);
        final WebsocketRegistry websocketRegistry = metaData.get(WEBSOCKET_REGISTRY);
        websocketRegistry.addConnection(entry);
    }
}
