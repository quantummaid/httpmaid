package de.quantummaid.httpmaid.documentation.marshalling;

import de.quantummaid.httpmaid.handler.http.HttpHandler;
import de.quantummaid.httpmaid.handler.http.HttpRequest;
import de.quantummaid.httpmaid.handler.http.HttpResponse;

import java.util.Map;

//Showcase start marshallingHandler
public class MarshallingHandler implements HttpHandler {
    @Override
    public void handle(final HttpRequest request, final HttpResponse response) {
        final Map<String, Object> responseMap = Map.of(
                "orderId", "qwefgfd-gt-yeetgtr",
                "status", "SHIPPING");
        response.setBody(responseMap);
    }
}
//Showcase end marshallingHandler
