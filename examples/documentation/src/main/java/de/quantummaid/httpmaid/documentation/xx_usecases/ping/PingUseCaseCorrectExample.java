package de.quantummaid.httpmaid.documentation.xx_usecases.ping;

import de.quantummaid.httpmaid.HttpMaid;

import static de.quantummaid.httpmaid.HttpMaid.anHttpMaid;
import static de.quantummaid.httpmaid.purejavaendpoint.PureJavaEndpoint.pureJavaEndpointFor;

public class PingUseCaseCorrectExample {

    public static void main(String[] args) {
        //Showcase start pingUseCaseCorrectExample
        final HttpMaid httpMaid = anHttpMaid()
                .get("/ping", PingUseCase.class)
                .build();
        //Showcase end pingUseCaseCorrectExample
        pureJavaEndpointFor(httpMaid).listeningOnThePort(1337);
    }
}
