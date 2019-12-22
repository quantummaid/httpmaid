package de.quantummaid.httpmaid.documentation.xx_usecases.ping;

import de.quantummaid.httpmaid.HttpMaid;

import static de.quantummaid.httpmaid.HttpMaid.anHttpMaid;
import static de.quantummaid.httpmaid.purejavaendpoint.PureJavaEndpoint.pureJavaEndpointFor;

public class PingUseCaseLowLevelExample {

    public static void main(String[] args) {
        //Showcase start pingUseCaseLowLevel
        final HttpMaid httpMaid = anHttpMaid()
                .get("/ping", (request, response) -> {
                    final PingUseCase pingUseCase = new PingUseCase();
                    pingUseCase.ping();
                })
                .build();
        //Showcase end pingUseCaseLowLevel
        pureJavaEndpointFor(httpMaid).listeningOnThePort(1337);
    }
}
