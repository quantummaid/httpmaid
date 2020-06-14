package de.quantummaid.httpmaid.http;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import static de.quantummaid.httpmaid.util.Validators.validateNotNull;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Header {
    private final HeaderKey key;
    private final HeaderValue value;

    public static Header header(final HeaderKey key,
                                final HeaderValue value) {
        validateNotNull(key, "key");
        validateNotNull(value, "value");
        return new Header(key, value);
    }

    public HeaderKey key() {
        return key;
    }

    public HeaderValue value() {
        return value;
    }
}
