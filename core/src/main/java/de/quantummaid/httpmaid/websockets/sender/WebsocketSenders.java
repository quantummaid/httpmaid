package de.quantummaid.httpmaid.websockets.sender;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class WebsocketSenders {
    private final Map<WebsocketSenderId, WebsocketSender<Object>> senders;

    public static WebsocketSenders websocketSenders() {
        return new WebsocketSenders(new ConcurrentHashMap<>());
    }

    public void addWebsocketSender(final WebsocketSenderId websocketSenderId,
                                   final WebsocketSender<?> websocketSender) {
        senders.put(websocketSenderId, (WebsocketSender<Object>) websocketSender);
    }

    public WebsocketSender<Object> senderById(final WebsocketSenderId id) {
        return senders.get(id);
    }
}
