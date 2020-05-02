package de.quantummaid.httpmaid.websockets.backchannel;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Websockets {

    public static Websockets websockets() {
        return new Websockets();
    }

    public void sendToAllWebsocketsAuthenticatedAs(final Object authenticationInformation,
                                                   final String message) {
        throw new UnsupportedOperationException();
    }

    public void sendToAllWebsockets(final String message) {
        throw new UnsupportedOperationException();
    }

    public void sendToThisWebsocket(final String message) {
        throw new UnsupportedOperationException();
    }
}
