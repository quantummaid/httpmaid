package de.quantummaid.httpmaid.tests.specs.websockets.domain;

public final class BroadcastingUseCase {

    public void broadcast(final String message, final MyBroadcaster broadcaster) {
        broadcaster.send(message);
    }
}
