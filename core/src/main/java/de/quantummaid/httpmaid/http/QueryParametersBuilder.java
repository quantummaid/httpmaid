/*
 * Copyright (c) 2020 Richard Hauswald - https://quantummaid.de/.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

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
