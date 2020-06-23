package de.quantummaid.httpmaid.documentation.endpoints;

//Showcase start lambdaFunctionSample
import de.quantummaid.httpmaid.HttpMaid;
import de.quantummaid.httpmaid.awslambda.AwsLambdaEndpoint;

import java.util.Map;

import static de.quantummaid.httpmaid.HttpMaid.anHttpMaid;
import static de.quantummaid.httpmaid.awslambda.AwsLambdaEndpoint.awsLambdaEndpointFor;

public final class MyLambdaFunction {
    private static final HttpMaid HTTP_MAID = anHttpMaid()
            // ...
            .build();
    private static final AwsLambdaEndpoint ENDPOINT = awsLambdaEndpointFor(HTTP_MAID);

    public Map<String, Object> handleRequest(final Map<String, Object> event) {
        return ENDPOINT.delegate(event);
    }
}
//Showcase end lambdaFunctionSample
