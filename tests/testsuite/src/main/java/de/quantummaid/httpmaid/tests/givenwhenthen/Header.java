package de.quantummaid.httpmaid.tests.givenwhenthen;

import lombok.*;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Header {
    @Getter
    private final String name;
    @Getter
    private final String value;

    public static Header header(final String name,
                                final String value) {
        return new Header(name, value);
    }
}
