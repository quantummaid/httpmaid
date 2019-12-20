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

package de.quantummaid.httpmaid.cors;

import de.quantummaid.httpmaid.chains.MetaData;
import de.quantummaid.httpmaid.chains.Processor;
import de.quantummaid.httpmaid.cors.domain.Origin;
import de.quantummaid.httpmaid.cors.domain.RequestedHeaders;
import de.quantummaid.httpmaid.cors.domain.RequestedMethod;
import de.quantummaid.httpmaid.cors.policy.ResourceSharingPolicy;
import de.quantummaid.httpmaid.http.Http;
import de.quantummaid.httpmaid.util.Validators;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;

import static de.quantummaid.httpmaid.HttpMaidChainKeys.RESPONSE_HEADERS;
import static de.quantummaid.httpmaid.HttpMaidChainKeys.RESPONSE_STATUS;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class PreflightRequestProcessor implements Processor {
    private final ResourceSharingPolicy resourceSharingPolicy;

    public static Processor preflightRequestProcessor(final ResourceSharingPolicy resourceSharingPolicy) {
        Validators.validateNotNull(resourceSharingPolicy, "resourceSharingPolicy");
        return new PreflightRequestProcessor(resourceSharingPolicy);
    }

    @Override
    public void apply(final MetaData metaData) {
        metaData.set(RESPONSE_HEADERS, new HashMap<>());
        metaData.set(RESPONSE_STATUS, Http.StatusCodes.OK);
        // 1
        Origin.load(metaData).ifPresent(origin -> {
            // 2
            if(!resourceSharingPolicy.validateOrigin(origin)) {
                return;
            }
            // 3
            final RequestedMethod requestedMethod = RequestedMethod.load(metaData);
            // 4
            final RequestedHeaders requestedHeaders = RequestedHeaders.load(metaData);
            // 5
            if(!resourceSharingPolicy.validateRequestedMethod(requestedMethod)) {
                return;
            }
            // 6
            if(!resourceSharingPolicy.validateRequestedHeaders(requestedHeaders)) {
                return;
            }
            // 7
            final Map<String, String> responseHeaders = metaData.get(RESPONSE_HEADERS);
            responseHeaders.put(Cors.ACCESS_CONTROL_ALLOW_ORIGIN, origin.internalValueForMapping());
            if(resourceSharingPolicy.supportsCredentials()) {
                responseHeaders.put(Cors.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
            }
            // 8
            resourceSharingPolicy.maxAge().generateHeaderValue()
                    .ifPresent(maxAge -> responseHeaders.put(Cors.ACCESS_CONTROL_MAX_AGE, maxAge));
            // 9
            if(!requestedMethod.isSimpleMethod()) {
                responseHeaders.put(Cors.ACCESS_CONTROL_ALLOW_METHODS, requestedMethod.internalValueForMapping());
            }
            // 10
            requestedHeaders.generateHeaderValue()
                    .ifPresent(allowedHeaders -> responseHeaders.put(Cors.ACCESS_CONTROL_ALLOW_HEADERS, allowedHeaders));
        });
    }
}
