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

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import de.quantummaid.httpmaid.awslambda.AwsLambdaEndpoint;
import de.quantummaid.httpmaid.websockets.WebsocketsModule;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Map;

import static de.quantummaid.httpmaid.awslambda.AwsLambdaEndpoint.awsLambdaEndpointFor;
import static de.quantummaid.httpmaid.awslambda.registry.DynamoDbWebsocketRegistry.dynamoDbWebsocketRegistry;
import static de.quantummaid.httpmaid.chains.Configurator.configuratorForType;
import static de.quantummaid.httpmaid.remotespecsinstance.HttpMaidFactory.httpMaid;

@ToString
@EqualsAndHashCode
public final class TestLambda implements RequestHandler<Map<String, Object>, APIGatewayProxyResponseEvent> {
    private static final AwsLambdaEndpoint ENDPOINT = awsLambdaEndpointFor(
            httpMaid(httpMaidBuilder ->
                    httpMaidBuilder.configured(configuratorForType(
                            WebsocketsModule.class,
                            websocketsModule -> websocketsModule.setWebsocketRegistry(dynamoDbWebsocketRegistry()))))
    );

    @Override
    public APIGatewayProxyResponseEvent handleRequest(final Map<String, Object> event,
                                                      final Context context) {
        return ENDPOINT.delegate(event, context);
    }
}
