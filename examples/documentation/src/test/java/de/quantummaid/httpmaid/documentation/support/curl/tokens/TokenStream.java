package de.quantummaid.httpmaid.documentation.support.curl.tokens;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.List;

import static java.util.Arrays.asList;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class TokenStream {
    private final List<String> tokens;
    private int currentLocation = 0;

    public static TokenStream splitToTokenStream(final String string, final String splitRegex) {
        final String[] split = string.split(splitRegex);
        return new TokenStream(asList(split));
    }

    public static TokenStream tokenStream(final List<String> tokens) {
        return new TokenStream(tokens);
    }

    public boolean hasNext() {
        return currentLocation < tokens.size();
    }

    public String next() {
        final String token = tokens.get(currentLocation);
        currentLocation = currentLocation + 1;
        return token;
    }
}
