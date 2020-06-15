package de.quantummaid.httpmaid.http;

import java.util.ArrayList;
import java.util.List;

import static de.quantummaid.httpmaid.http.QueryParameter.QueryParameter;
import static de.quantummaid.httpmaid.http.QueryParameterName.queryParameterName;
import static de.quantummaid.httpmaid.http.QueryParameterValue.queryParameterValue;

public class QueryParametersBuilder {
    final List<QueryParameter> parameters = new ArrayList<>();

    public QueryParametersBuilder withParameter(final String name, final List<String> values) {
        final QueryParameterName queryParameterName = queryParameterName(name);
        for (final String value : values) {
            parameters.add(QueryParameter(queryParameterName, queryParameterValue(value)));
        }
        return this;
    }

    public QueryParameters build() {
        return QueryParameters.queryParameters(parameters);
    }
}
