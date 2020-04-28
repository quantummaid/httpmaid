package de.quantummaid.httpmaid.documentation.usecases.calculation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

@ToString
@EqualsAndHashCode(callSuper = false)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class JsonMatcher extends TypeSafeMatcher<String> {
    private final String expected;

    public static TypeSafeMatcher<String> isJson(final String expected) {
        return new JsonMatcher(expected);
    }

    @Override
    protected boolean matchesSafely(final String actual) {
        final JsonNode actualJson = parseJson(actual);
        final JsonNode expectedJson = parseJson(expected);
        return actualJson.equals(expectedJson);
    }

    @Override
    public void describeTo(final Description description) {
        description.appendText(expected);
    }

    private static JsonNode parseJson(final String json) {
        final ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readTree(json);
        } catch (final JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
