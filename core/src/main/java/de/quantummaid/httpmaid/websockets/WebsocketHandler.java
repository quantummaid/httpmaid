package de.quantummaid.httpmaid.websockets;


import de.quantummaid.httpmaid.chains.MetaData;
import de.quantummaid.httpmaid.handler.Handler;
import de.quantummaid.httpmaid.handler.http.HttpRequest;
import de.quantummaid.httpmaid.websockets.backchannel.Websockets;

import static de.quantummaid.httpmaid.websockets.backchannel.Websockets.websockets;

public interface WebsocketHandler extends Handler {

    @Override
    default void handle(final MetaData metaData) {
        final HttpRequest httpRequest = HttpRequest.httpRequest(metaData);
        final Websockets websockets = websockets();
        handle(httpRequest, websockets);
    }

    void handle(HttpRequest request, Websockets websockets);
}
