package de.quantummaid.httpmaid.websockets.sender;

import de.quantummaid.httpmaid.chains.MetaDataKey;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import static de.quantummaid.httpmaid.chains.MetaDataKey.metaDataKey;
import static de.quantummaid.httpmaid.util.Validators.validateNotNull;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class WebsocketSenderId {
    public static final MetaDataKey<WebsocketSenderId> WEBSOCKET_SENDER_ID = metaDataKey("WEBSOCKET_SENDER_ID");

    private final String id;

    public static WebsocketSenderId websocketSenderId(final String id) {
        validateNotNull(id, "id");
        return new WebsocketSenderId(id);
    }
}
