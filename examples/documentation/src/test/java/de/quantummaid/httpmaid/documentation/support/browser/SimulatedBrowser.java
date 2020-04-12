package de.quantummaid.httpmaid.documentation.support.browser;

import de.quantummaid.httpmaid.client.HttpClientRequestBuilder;
import de.quantummaid.httpmaid.client.HttpMaidClient;
import de.quantummaid.httpmaid.client.SimpleHttpResponseObject;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.quantummaid.httpmaid.client.HttpClientRequest.aGetRequestToThePath;
import static java.lang.String.format;
import static java.util.stream.Collectors.joining;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class SimulatedBrowser implements Browser {
    private final HttpMaidClient client;
    private String currentPageContent;
    private final List<String> cookies = new ArrayList<>();
    private final Map<String, String> injectedCookies = new HashMap<>();

    public static Browser simulatedBrowser(final HttpMaidClient client) {
        return new SimulatedBrowser(client);
    }

    @Override
    public void request(final String route) {
        final HttpClientRequestBuilder<SimpleHttpResponseObject> requestBuilder = aGetRequestToThePath(route);
        if (!injectedCookies.isEmpty()) {
            final String cookiesHeader = injectedCookies.entrySet().stream()
                    .map(entryValue -> format("%s=\"%s\"", entryValue.getKey(), entryValue.getValue()))
                    .collect(joining("; "));
            requestBuilder.withHeader("Cookie", cookiesHeader);
        }

        final SimpleHttpResponseObject response = client.issue(requestBuilder);

        final int statusCode = response.getStatusCode();
        assertThat(statusCode, is(200));
        this.currentPageContent = response.getBody();

        final Map<String, String> headers = response.getHeaders();
        final String cookieHeader = headers.get("set-cookie");
        cookies.add(cookieHeader);
    }

    @Override
    public void injectCookie(final String key, final String value) {
        injectedCookies.put(key, value);
    }

    @Override
    public void assertCurrentPageContains(String content) {
        assertThat(currentPageContent, containsString(content));
    }

    @Override
    public void assertCookieExists(final String cookie) {
        assertThat(cookies, containsInAnyOrder(cookie));
    }
}
