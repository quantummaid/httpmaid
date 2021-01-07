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

package de.quantummaid.httpmaid.awslambdacognitoauthorizer;

import de.quantummaid.httpmaid.chains.MetaData;
import de.quantummaid.httpmaid.handler.http.HttpRequest;
import de.quantummaid.httpmaid.websockets.additionaldata.AdditionalWebsocketDataProvider;
import software.amazon.awssdk.services.cognitoidentityprovider.model.GetUserResponse;

import java.util.Map;

import static de.quantummaid.httpmaid.awslambdacognitoauthorizer.CognitoWebsocketAuthorizer.AUTHORIZATION_TOKEN;
import static de.quantummaid.httpmaid.awslambdacognitoauthorizer.CognitoWebsocketAuthorizer.GET_USER_RESPONSE;

public interface CognitoContextEnricher extends AdditionalWebsocketDataProvider {
    Map<String, Object> enrich(HttpRequest request,
                               GetUserResponse getUserResponse,
                               Map<String, Object> authorizationToken);

    @Override
    default Map<String, Object> provide(final HttpRequest request) {
        final MetaData metaData = request.getMetaData();
        final GetUserResponse getUserResponse = metaData.get(GET_USER_RESPONSE);
        final Map<String, Object> authorizationToken = metaData.get(AUTHORIZATION_TOKEN);
        return enrich(request, getUserResponse, authorizationToken);
    }
}
