package de.quantummaid.httpmaid.websockets.processors;

import de.quantummaid.httpmaid.chains.MetaData;
import de.quantummaid.httpmaid.chains.Processor;
import de.quantummaid.httpmaid.http.Headers;
import de.quantummaid.httpmaid.websockets.registry.WebsocketRegistry;
import de.quantummaid.httpmaid.websockets.registry.WebsocketRegistryEntry;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import static de.quantummaid.httpmaid.HttpMaidChainKeys.REQUEST_HEADERS;
import static de.quantummaid.httpmaid.websockets.WebsocketMetaDataKeys.*;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class RestoreWebsocketContextInformationProcessor implements Processor {

    public static Processor restoreWebsocketContextInformationProcessor() {
        return new RestoreWebsocketContextInformationProcessor();
    }

    @Override
    public void apply(final MetaData metaData) {
        final Boolean isWebsocketMessage = metaData.getOptional(REQUEST_TYPE)
                .map(WEBSOCKET_MESSAGE::equals)
                .orElse(false);
        if (isWebsocketMessage) {
            final WebsocketRegistry websocketRegistry = metaData.get(WEBSOCKET_REGISTRY);
            final Object connectionInformation = metaData.get(WEBSOCKET_CONNECTION_INFORMATION);
            final WebsocketRegistryEntry entry = websocketRegistry.byConnectionInformation(connectionInformation);
            entry.restoreToMetaData(metaData);
        }
    }
}
