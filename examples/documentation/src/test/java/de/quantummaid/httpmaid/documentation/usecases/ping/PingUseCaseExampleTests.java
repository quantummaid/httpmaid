package de.quantummaid.httpmaid.documentation.usecases.ping;

import de.quantummaid.httpmaid.HttpMaid;
import de.quantummaid.httpmaid.documentation.support.Deployer;
import org.junit.jupiter.api.Test;

import static de.quantummaid.httpmaid.HttpMaid.anHttpMaid;
import static de.quantummaid.httpmaid.client.HttpClientRequest.aGetRequestToThePath;

public class PingUseCaseExampleTests {

    @Test
    public void pingUseCaseLowLevelExample() {
        //Showcase start pingUseCaseLowLevel
        final HttpMaid httpMaid = anHttpMaid()
                .get("/ping", (request, response) -> {
                    final PingUseCase pingUseCase = new PingUseCase();
                    pingUseCase.ping();
                })
                .build();
        //Showcase end pingUseCaseLowLevel
        Deployer.test(httpMaid, client -> client.issue(aGetRequestToThePath("/ping")));
    }

    @Test
    public void pingUseCaseCorrectExample() {
        //Showcase start pingUseCaseCorrectExample
        final HttpMaid httpMaid = anHttpMaid()
                .get("/ping", PingUseCase.class)
                .build();
        //Showcase end pingUseCaseCorrectExample
        Deployer.test(httpMaid, client -> client.issue(aGetRequestToThePath("/ping")));
    }
}
