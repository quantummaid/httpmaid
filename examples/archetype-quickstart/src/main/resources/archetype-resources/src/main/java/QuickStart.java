package $package;

import de.quantummaid.httpmaid.HttpMaid;
import de.quantummaid.httpmaid.purejavaendpoint.PureJavaEndpoint;

import static de.quantummaid.httpmaid.HttpMaid.anHttpMaid;
import static de.quantummaid.httpmaid.purejavaendpoint.PureJavaEndpoint.pureJavaEndpointFor;

public class QuickStart {
    public static void main(String[] args) {

        final HttpMaid httpMaid = anHttpMaid()
                .get("/hello", (request, response) -> response.setBody("hi!"))
                .build();
        final PureJavaEndpoint endpoint = pureJavaEndpointFor(httpMaid).listeningOnThePort(1337);
    }
}
