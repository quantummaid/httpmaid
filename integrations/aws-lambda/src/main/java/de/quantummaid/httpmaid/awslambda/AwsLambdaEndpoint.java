/*
 * Copyright (c) 2019 Richard Hauswald - https://quantummaid.de/.
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
import de.quantummaid.httpmaid.chains.MetaData;
import de.quantummaid.httpmaid.chains.MetaDataKey;
import de.quantummaid.httpmaid.logger.LoggerImplementation;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static de.quantummaid.httpmaid.HttpMaidChainKeys.*;
import static de.quantummaid.httpmaid.awslambda.AwsLambdaLogger.awsLambdaLogger;
import static de.quantummaid.httpmaid.chains.MetaData.emptyMetaData;
import static de.quantummaid.httpmaid.chains.MetaDataKey.metaDataKey;
import static de.quantummaid.httpmaid.util.Maps.mapToMultiMap;
import static de.quantummaid.httpmaid.util.Streams.inputStreamToString;
import static de.quantummaid.httpmaid.util.Streams.stringToInputStream;
import static de.quantummaid.httpmaid.util.Validators.validateNotNull;
import static java.util.Optional.ofNullable;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class AwsLambdaEndpoint {

    public static final MetaDataKey<Context> CONTEXT_KEY = metaDataKey("awsLambdaContext");

    private final HttpMaid httpMaid;

    public static LoggerImplementation awsLogger() {
        return awsLambdaLogger();
    }

    public static AwsLambdaEndpoint awsLambdaEndpointFor(final HttpMaid httpMaid) {
        validateNotNull(httpMaid, "httpMaid");
        return new AwsLambdaEndpoint(httpMaid);
    }

    public APIGatewayProxyResponseEvent delegate(final APIGatewayProxyRequestEvent event, final Context context) {
        final String httpRequestMethod = event.getHttpMethod();
        final String path = event.getPath();
        final Map<String, String> headers = event.getHeaders();
        final String body = ofNullable(event.getBody()).orElse("");
        final InputStream bodyStream = stringToInputStream(body);
        final Map<String, String> queryParameters = ofNullable(event.getQueryStringParameters()).orElseGet(HashMap::new);

        final MetaData metaData = emptyMetaData();
        metaData.set(RAW_REQUEST_HEADERS, mapToMultiMap(headers));
        metaData.set(RAW_REQUEST_QUERY_PARAMETERS, queryParameters);
        metaData.set(RAW_METHOD, httpRequestMethod);
        metaData.set(RAW_PATH, path);
        metaData.set(REQUEST_BODY_STREAM, bodyStream);
        metaData.set(CONTEXT_KEY, context);
        metaData.set(IS_HTTP_REQUEST, true);

        httpMaid.handleRequest(metaData, response -> {
            throw new UnsupportedOperationException();
        });

        final int statusCode = metaData.get(RESPONSE_STATUS);
        final Map<String, String> responseHeaders = metaData.get(RESPONSE_HEADERS);
        final InputStream responseStream = metaData.get(RESPONSE_STREAM);
        final String responseBody = inputStreamToString(responseStream);
        return new APIGatewayProxyResponseEvent().withStatusCode(statusCode).withHeaders(responseHeaders).withBody(responseBody);
    }
}
