package de.quantummaid.httpmaid.jsr356;

import javax.websocket.Session;
import java.io.IOException;

import static de.quantummaid.httpmaid.jsr356.Jsr356Exception.jsr356Exception;

final class SenderHelper {

    static void sendMessage(final Session session,
                            final String message) {
        try {
            session.getBasicRemote().sendText(message);
        } catch (final IOException e) {
            throw jsr356Exception(e);
        }
    }
}
