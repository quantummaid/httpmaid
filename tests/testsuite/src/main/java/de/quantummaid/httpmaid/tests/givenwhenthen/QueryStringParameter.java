package de.quantummaid.httpmaid.tests.givenwhenthen;

import de.quantummaid.httpmaid.util.Validators;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class QueryStringParameter {
    private final String name;
    private final String value;

    public static QueryStringParameter queryStringParameter(final String name, final String value) {
        Validators.validateNotNullNorEmpty(name, "name");
        Validators.validateNotNull(value, "value");
        return new QueryStringParameter(name, value);
    }

    public String name() {
        return name;
    }

    public String value() {
        return value;
    }
}
