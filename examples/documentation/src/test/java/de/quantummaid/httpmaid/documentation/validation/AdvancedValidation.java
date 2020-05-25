package de.quantummaid.httpmaid.documentation.validation;

import de.quantummaid.httpmaid.HttpMaid;
import de.quantummaid.httpmaid.client.SimpleHttpResponseObject;
import de.quantummaid.httpmaid.documentation.support.Deployer;
import de.quantummaid.httpmaid.mapmaid.MapMaidConfigurators;
import org.junit.jupiter.api.Test;

import static de.quantummaid.httpmaid.HttpMaid.anHttpMaid;
import static de.quantummaid.httpmaid.client.HttpClientRequest.aPostRequestToThePath;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public final class AdvancedValidation {

    @Test
    public void setValidationErrorStatusCode() {
        //Showcase start setValidationErrorStatusCode
        final HttpMaid httpMaid = anHttpMaid()
                .post("/", SomeUseCase.class)
                .configured(MapMaidConfigurators.toConfigureMapMaidUsingRecipe(mapMaidBuilder ->
                        mapMaidBuilder.withExceptionIndicatingValidationError(SomeValidationException.class)))
                .configured(MapMaidConfigurators.toSetStatusCodeOnMapMaidValidationErrorsTo(401))
                .build();
        //Showcase end setValidationErrorStatusCode

        Deployer.test(httpMaid, httpMaidClient -> {
            final SimpleHttpResponseObject response = httpMaidClient.issue(aPostRequestToThePath("/").withTheBody("{}"));
            assertThat(response.getStatusCode(), is(401));
        });
    }
}
