package de.quantummaid.httpmaid.tests.givenwhenthen;

import de.quantummaid.httpmaid.tests.givenwhenthen.checkpoints.Checkpoints;
import de.quantummaid.httpmaid.tests.givenwhenthen.client.HttpClientResponse;
import de.quantummaid.httpmaid.tests.givenwhenthen.client.HttpClientWrapper;
import de.quantummaid.httpmaid.tests.givenwhenthen.client.WrappedWebsocket;
import lombok.*;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class TestData {
    @Getter
    @Setter
    private HttpClientResponse response;
    @Getter
    @Setter
    private Throwable initializationException;
    @Getter
    @Setter
    private Checkpoints checkpoints;
    @Getter
    @Setter
    private HttpClientWrapper clientWrapper;
    @Getter
    @Setter
    private WrappedWebsocket websocket;

    public static TestData testData() {
        return new TestData();
    }
}
