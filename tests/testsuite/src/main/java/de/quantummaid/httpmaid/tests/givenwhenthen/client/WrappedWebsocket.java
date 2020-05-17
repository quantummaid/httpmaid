package de.quantummaid.httpmaid.tests.givenwhenthen.client;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.function.Consumer;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class WrappedWebsocket {
    private final Consumer<String> sender;

    public static WrappedWebsocket wrappedWebsocket(final Consumer<String> sender) {
        return new WrappedWebsocket(sender);
    }

    public void send(final String message) {
        sender.accept(message);
    }
}
