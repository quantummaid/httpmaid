package de.quantummaid.httpmaid.documentation.usecases.dependencyInjection;

import de.quantummaid.httpmaid.HttpMaid;
import de.quantummaid.httpmaid.usecases.instantiation.UseCaseInstantiator;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static de.quantummaid.httpmaid.HttpMaid.anHttpMaid;
import static de.quantummaid.httpmaid.usecases.UseCaseConfigurators.toCreateUseCaseInstancesUsing;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public final class StartupChecksExampleTests {

    @Test
    public void disableStartupChecksExample() {
        final AtomicInteger integer = new AtomicInteger();
        final UseCaseInstantiator injector = new UseCaseInstantiator() {
            @Override
            public <T> T instantiate(Class<T> type) {
                integer.incrementAndGet();
                return null;
            }
        };
        //Showcase start disableStartupChecksExample
        final HttpMaid httpMaid = anHttpMaid()
                /*...*/
                .configured(toCreateUseCaseInstancesUsing(injector))
                .disableStartupChecks()
                .build();
        //Showcase end disableStartupChecksExample
        httpMaid.close();

        assertThat(integer.get(), is(0));
    }
}
