package de.quantummaid.httpmaid.documentation.handlingrequests;


import de.quantummaid.httpmaid.handler.http.HttpHandler;
import de.quantummaid.httpmaid.handler.http.HttpRequest;
import de.quantummaid.httpmaid.handler.http.HttpResponse;

//Showcase start helloHandler
public final class HelloHandler implements HttpHandler {

    @Override
    public void handle(final HttpRequest request, final HttpResponse response) {
        response.setBody("hi!");
    }
}
//Showcase end helloHandler
