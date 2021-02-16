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

package de.quantummaid.httpmaid.awslambda;

import de.quantummaid.httpmaid.websockets.registry.ConnectionInformation;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import static de.quantummaid.httpmaid.util.Validators.validateNotNull;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class AwsWebsocketConnectionInformation implements ConnectionInformation {
    public final String connectionId;
    public final String stage;
    public final String apiId;
    public final String region;

    public static AwsWebsocketConnectionInformation restore(final String serialized) {
        final String[] split = serialized.split("/");
        final String connectionId = split[0];
        final String stage = split[1];
        final String apiId = split[2];
        final String region = split[3];
        return new AwsWebsocketConnectionInformation(connectionId, stage, apiId, region);
    }

    public static AwsWebsocketConnectionInformation awsWebsocketConnectionInformation(final String connectionId,
                                                                                      final String stage,
                                                                                      final String apiId,
                                                                                      final String region) {
        validateNotNull(connectionId, "connectionId");
        validateNotNull(stage, "stage");
        validateNotNull(apiId, "apiId");
        validateNotNull(region, "region");
        return new AwsWebsocketConnectionInformation(connectionId, stage, apiId, region);
    }

    public String toEndpointUrl() {
        final String domainName = String.format("%s.execute-api.%s.amazonaws.com", apiId, region);
        return String.format("https://%s/%s", domainName, stage);
    }

    @Override
    public String uniqueIdentifier() {
        return connectionId + "/" + stage + "/" + apiId + "/" + region;
    }
}
