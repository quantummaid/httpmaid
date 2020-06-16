package de.quantummaid.httpmaid.http;

import de.quantummaid.httpmaid.util.Validators;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@ToString
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class QueryParameter {
    private final QueryParameterName name;
    private final QueryParameterValue value;

    public static QueryParameter queryParameter(final QueryParameterName name, final QueryParameterValue value) {
        Validators.validateNotNull(name, "name");
        Validators.validateNotNull(value, "value");
        return new QueryParameter(name, value);
    }

    public QueryParameterName name() {
        return name;
    }

    public QueryParameterValue value() {
        return value;
    }
}
