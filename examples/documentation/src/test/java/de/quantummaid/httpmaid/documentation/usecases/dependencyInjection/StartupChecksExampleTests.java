package de.quantummaid.httpmaid.documentation.usecases.dependencyInjection;

import de.quantummaid.httpmaid.HttpMaid;
import org.junit.jupiter.api.Test;

import static de.quantummaid.httpmaid.HttpMaid.anHttpMaid;

public final class StartupChecksExampleTests {

    @Test
    public void disableStartupChecksExample() {
        //Showcase start disableStartupChecksExample
        final HttpMaid httpMaid = anHttpMaid()
                /*...*/
                .disableStartupChecks()
                .build();
        //Showcase end disableStartupChecksExample
        httpMaid.close();
    }
}
