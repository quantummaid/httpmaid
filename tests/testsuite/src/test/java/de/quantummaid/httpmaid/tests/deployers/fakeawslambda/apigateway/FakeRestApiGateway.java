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

package de.quantummaid.httpmaid.tests.deployers.fakeawslambda.apigateway;

import de.quantummaid.httpmaid.awslambda.AwsLambdaEndpoint;
import de.quantummaid.httpmaid.tests.deployers.fakeawslambda.Server;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Map;

import static de.quantummaid.httpmaid.tests.deployers.fakeawslambda.apigateway.ApiGatewayUtils.mapRequestToRestApiEvent;
import static de.quantummaid.httpmaid.tests.deployers.fakeawslambda.apigateway.ApiGatewayUtils.mapRestApiEventToResponse;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class FakeRestApiGateway implements AutoCloseable {
    private final Server server;

    public static FakeRestApiGateway fakeRestApiGateway(final AwsLambdaEndpoint endpoint,
                                                        final int port) {
        final Server server = Server.server(port, exchange -> {
            try {
                final Map<String, Object> requestEvent = mapRequestToRestApiEvent(exchange);
                final Map<String, Object> responseEvent = endpoint.delegate(requestEvent);
                mapRestApiEventToResponse(responseEvent, exchange);
            } catch (final Exception e) {
                e.printStackTrace(System.err);
                throw e;
            }
        });
        return new FakeRestApiGateway(server);
    }

    @Override
    public void close() {
        server.close();
    }
}
