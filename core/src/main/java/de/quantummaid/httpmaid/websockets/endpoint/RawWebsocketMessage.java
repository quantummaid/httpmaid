package de.quantummaid.httpmaid.websockets.endpoint;

import de.quantummaid.httpmaid.chains.MetaData;
import de.quantummaid.httpmaid.websockets.WebsocketSender;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import static de.quantummaid.httpmaid.HttpMaidChainKeys.IS_HTTP_REQUEST;
import static de.quantummaid.httpmaid.HttpMaidChainKeys.REQUEST_BODY_STRING;
import static de.quantummaid.httpmaid.util.Validators.validateNotNull;
import static de.quantummaid.httpmaid.websockets.WebsocketMetaDataKeys.*;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class RawWebsocketMessage {
    private final Object connectionInformation;
    private final String body;
    private final WebsocketSender<Object> websocketSender;

    public static RawWebsocketMessage rawWebsocketMessage(final Object connectionInformation,
                                                          final String body,
                                                          final WebsocketSender<Object> sender) {
        validateNotNull(connectionInformation, "connectionInformation");
        validateNotNull(body, "body");
        validateNotNull(sender, "sender");
        return new RawWebsocketMessage(connectionInformation, body, sender);
    }

    public void enter(final MetaData metaData) {
        metaData.set(REQUEST_TYPE, WEBSOCKET_MESSAGE);
        metaData.set(IS_HTTP_REQUEST, false); // TODO
        metaData.set(WEBSOCKET_CONNECTION_INFORMATION, connectionInformation);
        metaData.set(REQUEST_BODY_STRING, body);
        metaData.set(WEBSOCKET_SENDER, websocketSender);
    }
}
