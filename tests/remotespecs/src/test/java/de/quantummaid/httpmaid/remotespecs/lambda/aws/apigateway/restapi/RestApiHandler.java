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

package de.quantummaid.httpmaid.remotespecs.lambda.aws.apigateway.restapi;

import software.amazon.awssdk.services.apigateway.ApiGatewayClient;
import software.amazon.awssdk.services.apigateway.model.*;

public final class RestApiHandler {

    private RestApiHandler() {
    }

    public static RestApiInformation loadRestApiInformation(final String apiName, final String region) {
        try (ApiGatewayClient apiGatewayClient = ApiGatewayClient.create()) {
            final String apiId = restApiIdByName(apiName, apiGatewayClient);
            final String stageName = restApiStageName(apiId, apiGatewayClient);
            return RestApiInformation.restApiInformation(apiId, stageName, region);
        }
    }

    private static String restApiIdByName(final String apiName,
                                          final ApiGatewayClient apiGatewayClient) {
        final GetRestApisResponse restApis = apiGatewayClient.getRestApis();
        final RestApi restApi = restApis.items().stream()
                .filter(api -> apiName.equals(api.name()))
                .findFirst()
                .orElseThrow();
        return restApi.id();
    }

    private static String restApiStageName(final String apiId,
                                           final ApiGatewayClient apiGatewayClient) {
        final GetStagesResponse stages = apiGatewayClient.getStages(GetStagesRequest.builder()
                .restApiId(apiId)
                .build());
        if (stages.item().size() != 1) {
            throw new UnsupportedOperationException();
        }
        final Stage restStage = stages.item().get(0);
        return restStage.stageName();
    }
}
