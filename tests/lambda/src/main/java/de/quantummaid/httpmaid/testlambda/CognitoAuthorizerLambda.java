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

package de.quantummaid.httpmaid.testlambda;

import de.quantummaid.httpmaid.awslambdacognitoauthorizer.CognitoLambdaAuthorizer;
import de.quantummaid.httpmaid.awslambdacognitoauthorizer.LambdaAuthorizer;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@ToString
@EqualsAndHashCode
@Slf4j
public final class CognitoAuthorizerLambda {
    private static final Router ROUTER = Router.router(createLambdaAuthorizer());

    public Map<String, Object> handleRequest(final Map<String, Object> event) {
        return ROUTER.route(event);
    }

    private static LambdaAuthorizer createLambdaAuthorizer() {
        final String region = System.getenv("REGION");
        final String poolId = System.getenv("POOL_ID");
        final String poolClientId = System.getenv("POOL_CLIENT_ID");
        return CognitoLambdaAuthorizer.cognitoLambdaAuthorizer(
                poolId,
                region,
                poolClientId,
                request -> request.queryParameters().parameter("access_token"),
                (request, event, getUserResponse, authorizationToken) -> Map.of("foo", "bar")
        );
    }
}
