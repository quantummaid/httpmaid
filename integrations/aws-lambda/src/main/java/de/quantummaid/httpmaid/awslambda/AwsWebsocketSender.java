package de.quantummaid.httpmaid.awslambda;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.apigatewaymanagementapi.AmazonApiGatewayManagementApi;
import com.amazonaws.services.apigatewaymanagementapi.AmazonApiGatewayManagementApiClient;
import com.amazonaws.services.apigatewaymanagementapi.model.PostToConnectionRequest;
import de.quantummaid.httpmaid.websockets.WebsocketSender;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.nio.ByteBuffer;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class AwsWebsocketSender implements WebsocketSender<AwsWebsocketConnectionInformation> {

    public static AwsWebsocketSender awsWebsocketSender() {
        return new AwsWebsocketSender();
    }

    @Override
    public void send(final AwsWebsocketConnectionInformation connectionInformation, final String message) {
        final String endpoint = connectionInformation.toEndpointUrl();
        System.out.println("endpoint = " + endpoint);

        final String region = connectionInformation.region();
        System.out.println("region = " + region);

        final String connectionId = connectionInformation.connectionId();
        System.out.println("connectionId = " + connectionId);

        final AwsClientBuilder.EndpointConfiguration config = new AwsClientBuilder.EndpointConfiguration(endpoint, region);

        final AmazonApiGatewayManagementApi amazonApiGatewayManagementApi = AmazonApiGatewayManagementApiClient.builder()
                .withEndpointConfiguration(config)
                .build();

        final ByteBuffer byteBuffer = ByteBuffer.wrap(message.getBytes());
        final PostToConnectionRequest postToConnectionRequest = new PostToConnectionRequest()
                .withConnectionId(connectionId)
                .withData(byteBuffer);

        amazonApiGatewayManagementApi.postToConnection(postToConnectionRequest);
    }
}
