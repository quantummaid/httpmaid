package de.quantummaid.httpmaid.http;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static de.quantummaid.httpmaid.http.Header.header;
import static de.quantummaid.httpmaid.http.HeaderName.headerKey;
import static de.quantummaid.httpmaid.http.HeaderValue.headerValue;
import static de.quantummaid.httpmaid.http.Headers.headers;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class HeadersBuilder {
    private final List<Header> headers;

    public static HeadersBuilder headersBuilder() {
        return new HeadersBuilder(new ArrayList<>());
    }

    public void withAdditionalHeader(final String key, final List<String> values) {
        values.forEach(value -> withAdditionalHeader(key, value));
    }

    public void withAdditionalHeader(final String key, final String value) {
        final Header header = header(headerKey(key), headerValue(value));
        headers.add(header);
    }

    public void withHeadersMap(final Map<String, List<String>> map) {
        map.forEach(this::withAdditionalHeader);
    }

    public Headers build() {
        return headers(headers);
    }
}
