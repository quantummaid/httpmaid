package de.quantummaid.httpmaid.http;

import java.util.ArrayList;
import java.util.List;

import static de.quantummaid.httpmaid.http.QueryParameter.queryParameter;
import static de.quantummaid.httpmaid.http.QueryParameterName.queryParameterName;
import static de.quantummaid.httpmaid.http.QueryParameterValue.queryParameterValue;

public class QueryParametersBuilder {
    final List<QueryParameter> parameters = new ArrayList<>();

    public QueryParametersBuilder withParameter(final String name, final List<String> values) {
        final QueryParameterName queryParameterName = queryParameterName(name);
        values.forEach(value -> addParameter(queryParameterName, value));
        return this;
    }

    public QueryParametersBuilder withParameter(final String name, final String value) {
        final QueryParameterName queryParameterName = queryParameterName(name);
        addParameter(queryParameterName, value);
        return this;
    }

    private void addParameter(final QueryParameterName name, final String value) {
        final QueryParameterValue queryParameterValue = queryParameterValue(value);
        parameters.add(queryParameter(name, queryParameterValue));
    }

    public QueryParameters build() {
        return QueryParameters.queryParameters(parameters);
    }
}
