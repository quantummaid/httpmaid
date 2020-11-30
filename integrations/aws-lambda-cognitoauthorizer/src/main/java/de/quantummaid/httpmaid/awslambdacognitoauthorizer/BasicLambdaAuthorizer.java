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

import de.quantummaid.httpmaid.awslambda.AwsLambdaEvent;
import de.quantummaid.httpmaid.chains.MetaData;
import de.quantummaid.httpmaid.http.HeadersBuilder;
import de.quantummaid.httpmaid.http.QueryParameters;
import de.quantummaid.httpmaid.http.QueryParametersBuilder;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.quantummaid.httpmaid.HttpMaidChainKeys.QUERY_PARAMETERS;
import static de.quantummaid.httpmaid.HttpMaidChainKeys.REQUEST_HEADERS;
import static de.quantummaid.httpmaid.awslambda.AwsLambdaEvent.AWS_LAMBDA_EVENT;
import static de.quantummaid.httpmaid.awslambda.AwsLambdaEvent.awsLambdaEvent;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class BasicLambdaAuthorizer implements LambdaAuthorizer {
    private final AuthorizationDecisionMaker authorizationDecisionMaker;

    public static BasicLambdaAuthorizer basicLambdaAuthorizer(final AuthorizationDecisionMaker decisionMaker) {
        return new BasicLambdaAuthorizer(decisionMaker);
    }

    @Override
    public Map<String, Object> delegate(final Map<String, Object> event) {
        log.debug("new authorization event {}", event);
        final AwsLambdaEvent awsLambdaEvent = awsLambdaEvent(event);
        final MetaData metaData = extractMetaData(awsLambdaEvent);
        final AuthorizationDecision authorizationDecision = authorizationDecisionMaker.isAuthorized(metaData);
        log.debug("made authorization decision: {}", authorizationDecision);
        final String methodArn = awsLambdaEvent.getAsString("methodArn");
        log.debug("extracted methodArn: {}", methodArn);
        final String serializedEvent = MapSerializer.toString(event);
        log.debug("serialized original lambda event for use in future calls: {}" + serializedEvent);
        final Map<String, Object> map = authorizationDecision.asMap(methodArn, Map.of("event", serializedEvent));
        log.debug("create final map to be returned by this lambda invocation: {}", map);
        return map;
    }

    private MetaData extractMetaData(final AwsLambdaEvent event) {
        final MetaData metaData = MetaData.emptyMetaData();
        final QueryParametersBuilder queryParametersBuilder = QueryParameters.builder();
        final Map<String, List<String>> queryParameters = event.getOrDefault("multiValueQueryStringParameters", HashMap::new);
        queryParameters.forEach(queryParametersBuilder::withParameter);
        metaData.set(QUERY_PARAMETERS, queryParametersBuilder.build());
        final Map<String, List<String>> headers = event.getOrDefault("multiValueHeaders", HashMap::new);
        final HeadersBuilder headersBuilder = HeadersBuilder.headersBuilder();
        headersBuilder.withHeadersMap(headers);
        metaData.set(REQUEST_HEADERS, headersBuilder.build());
        metaData.set(AWS_LAMBDA_EVENT, event);
        return metaData;
    }
}
