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

package de.quantummaid.httpmaid.remotespecs.lambda.aws.apigateway;

import de.quantummaid.httpmaid.tests.givenwhenthen.deploy.ApiBaseUrl;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import static de.quantummaid.httpmaid.tests.givenwhenthen.deploy.ApiBaseUrl.apiBaseUrl;
import static java.lang.String.format;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class HttpApiInformation {
    private static final int HTTPS_PORT = 443;

    private final String apiId;
    private final String region;

    public static HttpApiInformation httpApiInformation(final String apiId,
                                                        final String region) {
        return new HttpApiInformation(apiId, region);
    }

    public String host() {
        return format("%s.execute-api.%s.amazonaws.com", apiId, region);
    }

    public ApiBaseUrl baseUrl() {
        return apiBaseUrl("https", host(), HTTPS_PORT, "/");
    }
}
