package de.quantummaid.httpmaid.client;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import static de.quantummaid.httpmaid.util.Validators.validateNotNull;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Header {
    private final HeaderName name;
    private final HeaderValue value;

    public static Header header(final HeaderName name,
                                final HeaderValue value) {
        validateNotNull(name, "name");
        validateNotNull(value, "value");
        return new Header(name, value);
    }

    public String name() {
        return name.value();
    }

    public String value() {
        return value.value();
    }
}
