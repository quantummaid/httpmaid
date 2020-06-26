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

package de.quantummaid.httpmaid.remotespecs.lambda.aws.apigateway.websocketapi;

import software.amazon.awssdk.services.apigatewayv2.ApiGatewayV2Client;
import software.amazon.awssdk.services.apigatewayv2.model.*;

import static de.quantummaid.httpmaid.remotespecs.lambda.aws.apigateway.websocketapi.WebsocketApiInformation.websocketApiInformation;

public final class WebsocketApiHandler {

    private WebsocketApiHandler() {
    }

    public static WebsocketApiInformation loadWebsocketApiInformation(final String apiName) {
        try (ApiGatewayV2Client apiGatewayV2Client = ApiGatewayV2Client.create()) {
            final Api api = apiByName(apiName, apiGatewayV2Client);
            final String apiId = api.apiId();
            final String endpoint = api.apiEndpoint();
            final String region = endpoint.split("\\.")[2];
            final String stageName = stageNameByApi(api, apiGatewayV2Client);
            return websocketApiInformation(apiId, region, stageName);
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

    private static String stageNameByApi(final Api api,
                                         final ApiGatewayV2Client apiGatewayV2Client) {
        final String apiId = api.apiId();
        final GetStagesResponse websocketStages = apiGatewayV2Client.getStages(GetStagesRequest.builder()
                .apiId(apiId)
                .build());
        if (websocketStages.items().size() != 1) {
            throw new UnsupportedOperationException();
        }
        final Stage websocketStage = websocketStages.items().get(0);
        return websocketStage.stageName();
    }
}
