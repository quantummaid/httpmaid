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

package de.quantummaid.httpmaid.tests.givenwhenthen.builders;

import de.quantummaid.httpmaid.tests.givenwhenthen.Then;

public interface HeaderBuilder {

    HeaderBuilder withQueryStringParameter(String name, String value);

    default HeaderBuilder withDistinctCookieHeaders(final String... rawCookieHeaders) {
        return withHeaderOccuringMultipleTimesHavingDistinctValue("Cookie", rawCookieHeaders);
    }

    default HeaderBuilder withTheHeader(final String key, final String value) {
        return withHeaderOccuringMultipleTimesHavingDistinctValue(key, value);
    }

    default HeaderBuilder withCommaSeparatedMultiValueHeader(String key, String... values) {
        final String joinedValues = String.join(",", values);
        return withTheHeader(key, joinedValues);
    }

    default HeaderBuilder withContentType(String contentType) {
        return withTheHeader("Content-Type", contentType);
    }

    HeaderBuilder withHeaderOccuringMultipleTimesHavingDistinctValue(String key, String... values);

    Then isIssued();
}
