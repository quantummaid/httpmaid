package de.quantummaid.httpmaid.documentation.support.curl.tokens;

import java.util.ArrayList;
import java.util.List;

import static de.quantummaid.httpmaid.documentation.support.curl.tokens.TokenStream.tokenStream;

public final class BashTokens {

    private BashTokens() {
    }

    public static TokenStream bashTokensIn(final String input) {
        final TokenStream tokenStream = tokenStream(stringToCharacterList(input));
        final List<String> elements = new ArrayList<>(5);
        while (tokenStream.hasNext()) {
            final String element = nextElement(tokenStream);
            elements.add(element);
        }
        return tokenStream(elements);
    }

    private static String nextElement(final TokenStream tokenStream) {
        final StringBuilder stringBuilder = new StringBuilder();
        while (tokenStream.hasNext()) {
            final String currentToken = tokenStream.next();
            if (currentToken.matches("\\s")) {
                break;
            }
            if (currentToken.equals("\"")) {
                stringBuilder.append(parseInDoubleQuotes(tokenStream));
            } else if (currentToken.equals("'")) {
                stringBuilder.append(parseInSingleQuotes(tokenStream));
            } else {
                stringBuilder.append(currentToken);
            }
        }
        return stringBuilder.toString();
    }

    private static String parseInDoubleQuotes(final TokenStream tokenStream) {
        final StringBuilder stringBuilder = new StringBuilder();
        while (true) {
            final String next = tokenStream.next();
            if ("\"".equals(next)) {
                return stringBuilder.toString();
            }
            stringBuilder.append(next);
        }
    }

    private static String parseInSingleQuotes(final TokenStream tokenStream) {
        final StringBuilder stringBuilder = new StringBuilder();
        while (true) {
            final String next = tokenStream.next();
            if ("'".equals(next)) {
                return stringBuilder.toString();
            }
            stringBuilder.append(next);
        }
    }

    private static List<String> stringToCharacterList(final String string) {
        final char[] chars = string.toCharArray();
        final List<String> characterList = new ArrayList<>(chars.length);
        for (final char c : chars) {
            final String asString = "" + c;
            characterList.add(asString);
        }
        return characterList;
    }
}
