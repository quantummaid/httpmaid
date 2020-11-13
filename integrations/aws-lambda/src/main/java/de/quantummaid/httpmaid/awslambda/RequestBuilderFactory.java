package de.quantummaid.httpmaid.awslambda;

import de.quantummaid.httpmaid.endpoint.RawHttpRequestBuilder;

import static de.quantummaid.httpmaid.awslambda.AwsLambdaEvent.AWS_LAMBDA_EVENT;
import static de.quantummaid.httpmaid.endpoint.RawHttpRequest.rawHttpRequestBuilder;

public final class RequestBuilderFactory {

    private RequestBuilderFactory() {
    }

    public static RawHttpRequestBuilder createRequestBuilder(final AwsLambdaEvent event) {
        final RawHttpRequestBuilder builder = rawHttpRequestBuilder();
        builder.withAdditionalMetaData(AWS_LAMBDA_EVENT, event);
        return builder;
    }
}
