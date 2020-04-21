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

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import de.quantummaid.httpmaid.HttpMaid;
import de.quantummaid.httpmaid.chains.MetaDataKey;
import de.quantummaid.httpmaid.endpoint.RawRequestBuilder;
import de.quantummaid.httpmaid.logger.LoggerImplementation;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

import static de.quantummaid.httpmaid.awslambda.AwsLambdaLogger.awsLambdaLogger;
import static de.quantummaid.httpmaid.chains.MetaDataKey.metaDataKey;
import static de.quantummaid.httpmaid.endpoint.RawRequest.rawRequestBuilder;
import static de.quantummaid.httpmaid.util.Validators.validateNotNull;
import static java.util.Optional.ofNullable;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class AwsLambdaEndpoint {

    public static final MetaDataKey<Context> CONTEXT = metaDataKey("awsLambdaContext");

    private final HttpMaid httpMaid;

    public static LoggerImplementation awsLogger() {
        return awsLambdaLogger();
    }

    public static AwsLambdaEndpoint awsLambdaEndpointFor(final HttpMaid httpMaid) {
        validateNotNull(httpMaid, "httpMaid");
        return new AwsLambdaEndpoint(httpMaid);
    }

    public APIGatewayProxyResponseEvent delegate(final APIGatewayProxyRequestEvent event, final Context context) {
        return httpMaid.handleRequestSynchronously(() -> {
            final RawRequestBuilder builder = rawRequestBuilder();
            final String httpRequestMethod = event.getHttpMethod();
            builder.withMethod(httpRequestMethod);
            final Map<String, String> pathParameters = event.getPathParameters();
            final String path = pathParameters.get("path");
            builder.withPath(path);
            final Map<String, String> headers = event.getHeaders();
            builder.withUniqueHeaders(headers);
            final Map<String, String> queryParameters = ofNullable(event.getQueryStringParameters()).orElseGet(HashMap::new);
            builder.withQueryParameters(queryParameters);
            final String body = ofNullable(event.getBody()).orElse("");
            builder.withBody(body);
            builder.withAdditionalMetaData(CONTEXT, context);
            return builder.build();
        }, response -> {
            final int statusCode = response.status();
            final Map<String, String> responseHeaders = response.uniqueHeaders();
            final String responseBody = response.stringBody();
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(statusCode)
                    .withHeaders(responseHeaders)
                    .withBody(responseBody);
        });
    }
}
