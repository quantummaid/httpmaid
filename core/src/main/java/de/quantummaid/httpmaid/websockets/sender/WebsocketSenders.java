package de.quantummaid.httpmaid.websockets.sender;

import de.quantummaid.httpmaid.chains.MetaDataKey;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static de.quantummaid.httpmaid.chains.MetaDataKey.metaDataKey;
import static de.quantummaid.httpmaid.websockets.sender.NonSerializableWebsocketSender.NON_SERIALIZABLE_WEBSOCKET_SENDER;
import static de.quantummaid.httpmaid.websockets.sender.NonSerializableWebsocketSender.nonSerializableWebsocketSender;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class WebsocketSenders {
    public static final MetaDataKey<WebsocketSenders> WEBSOCKET_SENDERS = metaDataKey("WEBSOCKET_SENDERS");

    private final Map<WebsocketSenderId, WebsocketSender<Object>> senders;

    public static WebsocketSenders websocketSenders() {
        final WebsocketSenders websocketSenders = new WebsocketSenders(new ConcurrentHashMap<>());
        websocketSenders.addWebsocketSender(NON_SERIALIZABLE_WEBSOCKET_SENDER, nonSerializableWebsocketSender());
        return websocketSenders;
    }

    public void addWebsocketSender(final WebsocketSenderId websocketSenderId,
                                   final WebsocketSender<?> websocketSender) {
        senders.put(websocketSenderId, (WebsocketSender<Object>) websocketSender);
    }

    public WebsocketSender<Object> senderById(final WebsocketSenderId id) {
        return senders.get(id);
    }
}
