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

package de.quantummaid.httpmaid.remotespecs.lambda.aws.restapi;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import static java.lang.String.format;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class RestApiInformation {
    private final String apiId;
    private final String stageName;

    public static RestApiInformation restApiInformation(final String apiId,
                                                        final String stageName) {
        return new RestApiInformation(apiId, stageName);
    }

    public String renderUri(final String region) {
        return format("https://%s.execute-api.%s.amazonaws.com/%s", apiId, region, stageName);
    }

    public String host(final String region) {
        return format("%s.execute-api.%s.amazonaws.com", apiId, region);
    }

    public String basePath() {
        return String.format("/%s", stageName);
    }
}
