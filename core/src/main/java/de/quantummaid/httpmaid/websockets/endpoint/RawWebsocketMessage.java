package de.quantummaid.httpmaid.websockets.endpoint;

import de.quantummaid.httpmaid.chains.MetaData;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import static de.quantummaid.httpmaid.HttpMaidChainKeys.IS_HTTP_REQUEST;
import static de.quantummaid.httpmaid.HttpMaidChainKeys.REQUEST_BODY_STRING;
import static de.quantummaid.httpmaid.util.Validators.validateNotNull;
import static de.quantummaid.httpmaid.websockets.WebsocketMetaDataKeys.WEBSOCKET_CONNECTION_INFORMATION;
import static de.quantummaid.httpmaid.websockets.WebsocketMetaDataKeys.WEBSOCKET_MESSAGE;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class RawWebsocketMessage {
    private final Object connectionInformation;
    private final String body;

    public static RawWebsocketMessage rawWebsocketMessage(final Object connectionInformation,
                                                          final String body) {
        validateNotNull(connectionInformation, "connectionInformation");
        validateNotNull(body, "body");
        return new RawWebsocketMessage(connectionInformation, body);
    }

    public void enter(final MetaData metaData) {
        metaData.set(WEBSOCKET_MESSAGE, true);
        metaData.set(IS_HTTP_REQUEST, false); // TODO
        metaData.set(WEBSOCKET_CONNECTION_INFORMATION, connectionInformation);
        metaData.set(REQUEST_BODY_STRING, body);
    }
}
