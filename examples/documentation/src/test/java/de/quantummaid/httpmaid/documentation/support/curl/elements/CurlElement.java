package de.quantummaid.httpmaid.documentation.support.curl.elements;

import de.quantummaid.httpmaid.documentation.support.curl.CurlCommandBuilder;
import de.quantummaid.httpmaid.documentation.support.curl.tokens.TokenStream;

public interface CurlElement {

    boolean match(String token);

    void act(String token, TokenStream tokenStream, CurlCommandBuilder commandBuilder);
}
