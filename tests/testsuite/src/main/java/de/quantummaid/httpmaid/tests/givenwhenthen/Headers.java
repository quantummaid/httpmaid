package de.quantummaid.httpmaid.tests.givenwhenthen;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import static de.quantummaid.httpmaid.tests.givenwhenthen.Header.header;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Headers {
    private final List<Header> headers;

    public static Headers emptyHeaders() {
        return new Headers(new ArrayList<>());
    }

    public boolean containsName(final String key) {
        return headers.stream()
                .map(Header::getName)
                .map(String::toLowerCase)
                .anyMatch(key::equals);
    }

    public void add(final String name,
                    final String key) {
        headers.add(header(name, key));
    }

    public void forEach(BiConsumer<String, String> consumer) {
        headers.forEach(header -> {
                    final String name = header.getName();
                    final String value = header.getValue();
                    consumer.accept(name, value);
                });
    }
}
