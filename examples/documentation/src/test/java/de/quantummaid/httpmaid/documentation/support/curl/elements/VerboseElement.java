package de.quantummaid.httpmaid.documentation.support.curl.elements;

import de.quantummaid.httpmaid.documentation.support.curl.CurlCommandBuilder;
import de.quantummaid.httpmaid.documentation.support.curl.tokens.TokenStream;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class VerboseElement implements CurlElement {

    public static CurlElement verboseElement() {
        return new VerboseElement();
    }

    @Override
    public boolean match(final String token) {
        return "-v".equals(token);
    }

    @Override
    public void act(final String token,
                    final TokenStream tokenStream,
                    final CurlCommandBuilder commandBuilder) {
    }
}
