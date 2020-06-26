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

package de.quantummaid.httpmaid.remotespecs.lambda.aws.httpapi;

import software.amazon.awssdk.services.apigatewayv2.ApiGatewayV2Client;
import software.amazon.awssdk.services.apigatewayv2.model.Api;
import software.amazon.awssdk.services.apigatewayv2.model.GetApisResponse;

import static de.quantummaid.httpmaid.remotespecs.lambda.aws.httpapi.HttpApiInformation.httpApiInformation;

public final class HttpApiHandler {

    private HttpApiHandler() {
    }

    public static HttpApiInformation loadHttpApiInformation(final String apiName) {
        try (ApiGatewayV2Client apiGatewayV2Client = ApiGatewayV2Client.create()) {
            final Api api = apiByName(apiName, apiGatewayV2Client);
            final String apiId = api.apiId();
            final String endpoint = api.apiEndpoint();
            final String region = endpoint.split("\\.")[2];
            return httpApiInformation(apiId, region);
        }
    }

    private static Api apiByName(final String apiName,
                                 final ApiGatewayV2Client apiGatewayV2Client) {
        final GetApisResponse apis = apiGatewayV2Client.getApis();
        return apis.items().stream()
                .filter(api -> apiName.equals(api.name()))
                .findFirst()
                .orElseThrow();
    }
}
