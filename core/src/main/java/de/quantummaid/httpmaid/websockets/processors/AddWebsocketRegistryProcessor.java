package de.quantummaid.httpmaid.websockets.processors;

import de.quantummaid.httpmaid.chains.MetaData;
import de.quantummaid.httpmaid.chains.Processor;
import de.quantummaid.httpmaid.websockets.registry.WebsocketRegistry;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import static de.quantummaid.httpmaid.util.Validators.validateNotNull;
import static de.quantummaid.httpmaid.websockets.WebsocketMetaDataKeys.WEBSOCKET_REGISTRY;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class AddWebsocketRegistryProcessor implements Processor {
    private final WebsocketRegistry registry;

    public static Processor addWebsocketRegistryProcessor(final WebsocketRegistry registry) {
        validateNotNull(registry, "registry");
        return new AddWebsocketRegistryProcessor(registry);
    }

    @Override
    public void apply(final MetaData metaData) {
        metaData.set(WEBSOCKET_REGISTRY, registry);
    }
}
