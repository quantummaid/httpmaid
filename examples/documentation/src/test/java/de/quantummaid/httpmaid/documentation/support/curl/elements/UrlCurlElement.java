package de.quantummaid.httpmaid.documentation.support.curl.elements;

import de.quantummaid.httpmaid.documentation.support.curl.CurlCommandBuilder;
import de.quantummaid.httpmaid.documentation.support.curl.tokens.TokenStream;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.net.MalformedURLException;
import java.net.URL;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class UrlCurlElement implements CurlElement {

    public static CurlElement urlCurlElement() {
        return new UrlCurlElement();
    }

    @Override
    public boolean match(String token) {
        try {
            new URL(token);
            return true;
        } catch (final MalformedURLException e) {
            return false;
        }
    }

    @Override
    public void act(final String token,
                    final TokenStream tokenStream,
                    final CurlCommandBuilder commandBuilder) {
        try {
            final URL url = new URL(token);
            final String path = url.getPath();
            commandBuilder.setPath(path);
        } catch (final MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}
