package de.quantummaid.httpmaid.events.processors;

import de.quantummaid.httpmaid.chains.MetaData;
import de.quantummaid.httpmaid.chains.Processor;
import de.quantummaid.httpmaid.events.Event;
import de.quantummaid.httpmaid.websockets.broadcast.Broadcasters;
import de.quantummaid.httpmaid.websockets.sender.WebsocketSenders;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.List;

import static de.quantummaid.httpmaid.events.EventModule.EVENT;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class BroadcastingProcessor implements Processor {
    private final Broadcasters broadcasters;
    private final WebsocketSenders websocketSenders;

    public static BroadcastingProcessor broadcastingProcessor(final Broadcasters broadcasters,
                                                              final WebsocketSenders websocketSenders) {
        return new BroadcastingProcessor(broadcasters, websocketSenders);
    }

    @Override
    public void apply(final MetaData metaData) {
        final Event event = metaData.get(EVENT);
        final List<Object> broadcasterInstances = broadcasters.instantiateAll(metaData, websocketSenders);
        broadcasterInstances.forEach(event::addTypeInjection);
    }
}
