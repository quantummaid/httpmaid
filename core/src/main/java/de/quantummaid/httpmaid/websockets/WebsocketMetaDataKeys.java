package de.quantummaid.httpmaid.websockets;

import de.quantummaid.httpmaid.chains.MetaDataKey;
import de.quantummaid.httpmaid.websockets.registry.WebsocketRegistry;

import static de.quantummaid.httpmaid.chains.MetaDataKey.metaDataKey;

public final class WebsocketMetaDataKeys {
    public static final String WEBSOCKET_CONNECT = "WEBSOCKET_CONNECT";
    public static final String WEBSOCKET_MESSAGE = "WEBSOCKET_MESSAGE";

    public static final MetaDataKey<WebsocketRegistry> WEBSOCKET_REGISTRY = metaDataKey("WEBSOCKET_REGISTRY");
    public static final MetaDataKey<WebsocketSender<Object>> WEBSOCKET_SENDER = metaDataKey("WEBSOCKET_SENDER");

    public static final MetaDataKey<String> REQUEST_TYPE = metaDataKey("REQUEST_TYPE");
    public static final MetaDataKey<Object> WEBSOCKET_CONNECTION_INFORMATION = metaDataKey("WEBSOCKET_CONNECTION_INFORMATION");
    public static final MetaDataKey<String> WEBSOCKET_CATEGORY = metaDataKey("WEBSOCKET_CATEGORY");
}
