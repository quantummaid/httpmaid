package de.quantummaid.httpmaid.awslambda;

import de.quantummaid.httpmaid.websockets.sender.WebsocketSender;
import de.quantummaid.httpmaid.websockets.sender.WebsocketSenderId;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import static de.quantummaid.httpmaid.websockets.sender.WebsocketSenderId.websocketSenderId;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class AwsWebsocketSender implements WebsocketSender<AwsWebsocketConnectionInformation> {
    public static final WebsocketSenderId AWS_WEBSOCKET_SENDER = websocketSenderId("AWS_WEBSOCKET_SENDER");

    public static AwsWebsocketSender awsWebsocketSender() {
        return new AwsWebsocketSender();
    }

    @Override
    public void send(final AwsWebsocketConnectionInformation connectionInformation,
                     final String message) {
        throw new UnsupportedOperationException();
    }

    @Override
    public WebsocketSenderId senderId() {
        return AWS_WEBSOCKET_SENDER;
    }
}
