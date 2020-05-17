package de.quantummaid.httpmaid.websockets.broadcast;

import de.quantummaid.httpmaid.chains.MetaData;
import de.quantummaid.httpmaid.chains.MetaDataKey;
import de.quantummaid.httpmaid.websockets.registry.WebsocketRegistry;
import de.quantummaid.httpmaid.websockets.sender.WebsocketSenders;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.*;
import java.util.stream.Collectors;

import static de.quantummaid.httpmaid.chains.MetaDataKey.metaDataKey;
import static de.quantummaid.httpmaid.websockets.WebsocketMetaDataKeys.WEBSOCKET_REGISTRY;
import static de.quantummaid.httpmaid.websockets.broadcast.SerializingSender.serializingSender;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Broadcasters {
    public static final MetaDataKey<Broadcasters> BROADCASTERS = metaDataKey("BROADCASTERS");

    private final Map<Class<?>, BroadcasterFactory<?, Object>> factories = new HashMap<>();
    private final List<Class<?>> messageTypes = new ArrayList<>();

    public static Broadcasters broadcasters() {
        return new Broadcasters();
    }

    public <T, U> void addBroadcaster(final Class<T> type,
                                      final Class<U> messageType,
                                      final BroadcasterFactory<T, U> factory) {
        factories.put(type, (BroadcasterFactory<?, Object>) factory);
        messageTypes.add(messageType);
    }

    public Collection<Class<?>> injectionTypes() {
        return factories.keySet();
    }

    public List<Object> instantiateAll(final MetaData metaData, final WebsocketSenders websocketSenders) {
        final WebsocketRegistry websocketRegistry = metaData.get(WEBSOCKET_REGISTRY);
        final SerializingSender<Object> serializingSender = serializingSender(websocketRegistry, websocketSenders);
        return factories.values().stream()
                .map(broadcasterFactory -> broadcasterFactory.createBroadcaster(serializingSender))
                .collect(Collectors.toList());
    }
}
