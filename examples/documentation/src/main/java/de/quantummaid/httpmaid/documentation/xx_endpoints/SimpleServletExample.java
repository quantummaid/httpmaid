package de.quantummaid.httpmaid.documentation.xx_endpoints;

import de.quantummaid.httpmaid.HttpMaid;

import javax.servlet.http.HttpServlet;

import static de.quantummaid.httpmaid.HttpMaid.anHttpMaid;

public class SimpleServletExample {

    public static void main(String[] args) {

        final HttpMaid httpMaid = anHttpMaid()
                .build();

        //Showcase start servletSample
        //final HttpServlet servlet = servletEndpointFor(httpMaid); TODO: Methode gibts ned
        //Showcase end servletSample

        //Showcase start websocketServletSample
        //final HttpServlet servlet = webSocketAwareHttpMaidServlet(httpMaid); TODO: MEthode gibts ned
        //Showcase end websocketServletSample
    }
}
