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
public final class HeaderCurlElement implements CurlElement {

    public static CurlElement headerCurlElement() {
        return new HeaderCurlElement();
    }

    @Override
    public boolean match(final String token) {
        return "--header".equals(token);
    }

    @Override
    public void act(final String token,
                    final TokenStream tokenStream,
                    final CurlCommandBuilder commandBuilder) {
        final String header = tokenStream.next();
        final String[] split = header.split(": ");
        final String key = split[0];
        final String value = split[1];
        commandBuilder.addHeader(key, value);
    }
}
