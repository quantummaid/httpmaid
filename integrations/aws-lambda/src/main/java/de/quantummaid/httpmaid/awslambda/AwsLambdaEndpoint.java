package de.quantummaid.httpmaid.awslambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import de.quantummaid.httpmaid.HttpMaid;
import de.quantummaid.httpmaid.endpoint.RawHttpRequestBuilder;
import de.quantummaid.httpmaid.websockets.endpoint.RawWebsocketMessage;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.quantummaid.httpmaid.awslambda.AwsLambdaEvent.awsLambdaEvent;
import static de.quantummaid.httpmaid.awslambda.AwsLambdaEventKeys.*;
import static de.quantummaid.httpmaid.awslambda.AwsWebsocketConnectionInformation.awsWebsocketConnectionInformation;
import static de.quantummaid.httpmaid.endpoint.RawHttpRequest.rawHttpRequestBuilder;
import static de.quantummaid.httpmaid.util.Validators.validateNotNull;
import static de.quantummaid.httpmaid.websockets.endpoint.RawWebsocketConnect.rawWebsocketConnect;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class AwsLambdaEndpoint2 {
    private static final String CONNECT_EVENT_TYPE = "CONNECT";
    private static final String DISCONNECT_EVENT_TYPE = "DISCONNECT";
    private static final String MESSAGE_EVENT_TYPE = "MESSAGE";

    private final HttpMaid httpMaid;

    public static AwsLambdaEndpoint2 awsLambdaEndpointFor(final HttpMaid httpMaid) {
        validateNotNull(httpMaid, "httpMaid");
        return new AwsLambdaEndpoint2(httpMaid);
    }

    public APIGatewayProxyResponseEvent delegate(final Map<String, Object> event,
                                                 final Context context) {
        final AwsLambdaEvent awsLambdaEvent = awsLambdaEvent(event);
        if (awsLambdaEvent.isWebSocketRequest()) {
            return handleWebsocketRequest(awsLambdaEvent);
        } else {
            return handleNormalRequest(awsLambdaEvent);
        }
    }

    private APIGatewayProxyResponseEvent handleNormalRequest(final AwsLambdaEvent event) {
        return httpMaid.handleRequestSynchronously(() -> {
            final RawHttpRequestBuilder builder = rawHttpRequestBuilder();
            final String httpRequestMethod = event.getAsString(HTTP_METHOD);
            builder.withMethod(httpRequestMethod);
            final String path = event.getAsString(PATH);
            builder.withPath(path);
            final Map<String, List<String>> headers = event.getOrDefault(MULTIVALUE_HEADERS, HashMap::new);
            builder.withHeaders(headers);
            final Map<String, String> queryParameters = event.getOrDefault(QUERY_STRING_PARAMETERS, HashMap::new);
            builder.withQueryParameters(queryParameters);
            final String body = event.getOrDefault(BODY, "");
            builder.withBody(body);
            return builder.build();
        }, response -> {
            final int statusCode = response.status();
            final Map<String, String> responseHeaders = response.uniqueHeaders();
            final String responseBody = response.stringBody();
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(statusCode)
                    .withHeaders(responseHeaders)
                    .withBody(responseBody);
        });
    }

    private APIGatewayProxyResponseEvent handleWebsocketRequest(final AwsLambdaEvent event) {
        final String eventType = event.getFromContext("eventType");
        System.out.println("eventType = " + eventType);

        final String connectionId = event.getFromContext("connectionId");
        System.out.println("connectionId = " + connectionId);

        final String stage = event.getFromContext("stage");
        System.out.println("stage = " + stage);

        final String apiId = event.getFromContext("apiId");
        System.out.println("apiId = " + apiId);

        final String domainName = event.getFromContext("domainName");
        System.out.println("domainName = " + domainName);
        final String region = extractRegionFromDomain(domainName);
        System.out.println("region = " + region);

        final Object connectionInformation = awsWebsocketConnectionInformation(connectionId, stage, apiId, region);

        final APIGatewayProxyResponseEvent responseEvent = new APIGatewayProxyResponseEvent();
        if (CONNECT_EVENT_TYPE.equals(eventType)) {
            handleConnect(event, connectionInformation);
            return responseEvent;
        } else if (DISCONNECT_EVENT_TYPE.equals(eventType)) {
            httpMaid.handleWebsocketDisconnect();
            return responseEvent;
        } else if (MESSAGE_EVENT_TYPE.equals(eventType)) {
            return httpMaid.handleRequestSynchronously(() -> {
                final String body = event.getAsString("body");
                System.out.println("body = " + body);
                return RawWebsocketMessage.rawWebsocketMessage(connectionInformation, body);
            }, response -> {
                response.optionalStringBody()
                        .ifPresent(responseEvent::setBody);
                return responseEvent;
            });
        } else {
            throw new UnsupportedOperationException(String.format("Unsupported lambda event type '%s' with event '%s'", eventType, event));
        }
    }

    private void handleConnect(final AwsLambdaEvent event,
                               final Object connectionInformation) {
        httpMaid.handleRequest(() -> {
            final HashMap<String, String> queryParameters = event.getOrDefault(QUERY_STRING_PARAMETERS, HashMap::new);
            final Map<String, List<String>> headers = event.getOrDefault(MULTIVALUE_HEADERS, HashMap::new);
            return rawWebsocketConnect(connectionInformation, queryParameters, headers);
        }, response -> {
        });
    }

    private static String extractRegionFromDomain(final String domain) {
        return domain.split("\\.")[2];
    }
}
