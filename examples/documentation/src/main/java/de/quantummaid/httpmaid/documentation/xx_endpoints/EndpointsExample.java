package de.quantummaid.httpmaid.documentation.xx_endpoints;

import de.quantummaid.httpmaid.HttpMaid;

import static de.quantummaid.httpmaid.HttpMaid.anHttpMaid;
import static de.quantummaid.httpmaid.purejavaendpoint.PureJavaEndpoint.pureJavaEndpointFor;

public class EndpointsExample {

    public static void main(String[] args) {
        final HttpMaid httpMaid = anHttpMaid()
                .build();

        //Showcase start javaEndpoint
        pureJavaEndpointFor(httpMaid).listeningOnThePort(1337);
        //Showcase end javaEndpoint

        //Showcase start jettyEndpoint
        //jettyEndpointFor(httpMaid).listeningOnThePort(1337); // TODO: Methode gibts nicht
        //Showcase end jettyEndpoint

        //Showcase start jettyWebsocketsEndpoint
        //jettyEndpointWithWebSocketsSupportFor(httpMaid).listeningOnThePort(1337); //TODO: Methode gibts nicht
        //Showcase end jettyWebsocketsEndpoint
    }
}
