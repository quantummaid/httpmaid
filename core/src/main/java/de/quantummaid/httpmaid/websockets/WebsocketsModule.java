package de.quantummaid.httpmaid.websockets;

import de.quantummaid.httpmaid.chains.ChainExtender;
import de.quantummaid.httpmaid.chains.ChainModule;
import de.quantummaid.httpmaid.chains.ChainName;
import de.quantummaid.httpmaid.websockets.registry.WebsocketRegistry;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import static de.quantummaid.httpmaid.HttpMaidChains.*;
import static de.quantummaid.httpmaid.chains.ChainName.chainName;
import static de.quantummaid.httpmaid.chains.rules.Drop.drop;
import static de.quantummaid.httpmaid.chains.rules.Jump.jumpTo;
import static de.quantummaid.httpmaid.websockets.WebsocketMetaDataKeys.REQUEST_TYPE;
import static de.quantummaid.httpmaid.websockets.WebsocketMetaDataKeys.WEBSOCKET_CONNECT;
import static de.quantummaid.httpmaid.websockets.processors.AddWebsocketRegistryProcessor.addWebsocketRegistryProcessor;
import static de.quantummaid.httpmaid.websockets.processors.DetermineWebsocketCategoryProcessor.determineWebsocketCategoryProcessor;
import static de.quantummaid.httpmaid.websockets.processors.PutWebsocketInRegistryProcessor.putWebsocketInRegistryProcessor;
import static de.quantummaid.httpmaid.websockets.processors.RestoreWebsocketContextInformationProcessor.restoreWebsocketContextInformationProcessor;
import static de.quantummaid.httpmaid.websockets.registry.InMemoryRegistry.inMemoryRegistry;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class WebsocketsModule implements ChainModule {
    private static final ChainName CONNECT_WEBSOCKET = chainName("CONNECT_WEBSOCKET");

    private String categoryKey = "message";
    private WebsocketRegistry websocketRegistry = inMemoryRegistry();

    public static WebsocketsModule websocketsModule() {
        return new WebsocketsModule();
    }

    @Override
    public void register(final ChainExtender extender) {
        extender.appendProcessor(INIT, addWebsocketRegistryProcessor(websocketRegistry));
        extender.appendProcessor(PRE_PROCESS, restoreWebsocketContextInformationProcessor());
        extender.routeIfEquals(PRE_PROCESS, jumpTo(CONNECT_WEBSOCKET), REQUEST_TYPE, WEBSOCKET_CONNECT);
        extender.createChain(CONNECT_WEBSOCKET, drop(), jumpTo(EXCEPTION_OCCURRED));
        extender.appendProcessor(CONNECT_WEBSOCKET, putWebsocketInRegistryProcessor());
        extender.appendProcessor(PRE_DETERMINE_HANDLER, determineWebsocketCategoryProcessor(categoryKey));
    }
}
