package de.quantummaid.httpmaid.awslambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import de.quantummaid.httpmaid.HttpMaid;
import de.quantummaid.httpmaid.websockets.endpoint.RawWebsocketMessage;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.List;
import java.util.Map;

import static de.quantummaid.httpmaid.awslambda.AwsWebsocketConnectionInformation.awsWebsocketConnectionInformation;
import static de.quantummaid.httpmaid.awslambda.AwsWebsocketSender.awsWebsocketSender;
import static de.quantummaid.httpmaid.util.Validators.validateNotNull;

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

    public APIGatewayProxyResponseEvent handleRequest(final Map<String, Object> event,
                                                      final Context context) {
        System.out.println("event = " + event);

        final Map<String, Object> requestContext = (Map<String, Object>) event.get("requestContext");
        System.out.println("requestContext = " + requestContext);

        if (isWebSocketRequest(event)) {
            final String eventType = (String) requestContext.get("eventType");
            System.out.println("eventType = " + eventType);

            final String connectionId = (String) requestContext.get("connectionId");
            System.out.println("connectionId = " + connectionId);

            final String stage = (String) requestContext.get("stage");
            System.out.println("stage = " + stage);

            final String apiId = (String) requestContext.get("apiId");
            System.out.println("apiId = " + apiId);

            final String domainName = (String) requestContext.get("domainName");
            System.out.println("domainName = " + domainName);
            final String region = extractRegionFromDomain(domainName);
            System.out.println("region = " + region);

            final Object connectionInformation = awsWebsocketConnectionInformation(connectionId, stage, apiId, region);

            if (CONNECT_EVENT_TYPE.equals(eventType)) {
                handleConnect(event, connectionInformation);
            } else if (DISCONNECT_EVENT_TYPE.equals(eventType)) {
                httpMaid.handleWebsocketDisconnect();
            } else if (MESSAGE_EVENT_TYPE.equals(eventType)) {
                httpMaid.handleWebsocketMessage(() -> {
                    final String body = (String) event.get("body");
                    System.out.println("body = " + body);
                    final AwsWebsocketSender sender = awsWebsocketSender();
                    return RawWebsocketMessage.rawWebsocketMessage(connectionInformation, body, sender);
                });
            }
        }

        return new APIGatewayProxyResponseEvent().withBody("foobar");
    }

    private void handleConnect(final Map<String, Object> event,
                               final Object connectionInformation) {
        final Map<String, List<String>> multiValueHeaders = (Map<String, List<String>>) event.get("multiValueHeaders");
        System.out.println("multiValueHeaders = " + multiValueHeaders);
        httpMaid.handleWebsocketConnect(connectionInformation, multiValueHeaders);
    }

    private static boolean isWebSocketRequest(final Map<String, Object> event) {
        return !event.containsKey("httpMethod");
    }


    private static String extractRegionFromDomain(final String domain) {
        return domain.split("\\.")[2];
    }
}
