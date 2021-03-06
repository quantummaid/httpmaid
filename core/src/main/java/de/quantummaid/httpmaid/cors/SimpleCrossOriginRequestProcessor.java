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

package de.quantummaid.httpmaid.cors;

import de.quantummaid.httpmaid.chains.MetaData;
import de.quantummaid.httpmaid.chains.Processor;
import de.quantummaid.httpmaid.cors.policy.ResourceSharingPolicy;
import de.quantummaid.httpmaid.http.ResponseHeaders;
import de.quantummaid.httpmaid.util.Validators;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import static de.quantummaid.httpmaid.HttpMaidChainKeys.RESPONSE_HEADERS;
import static de.quantummaid.httpmaid.cors.Cors.*;
import static de.quantummaid.httpmaid.cors.domain.Origin.load;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class SimpleCrossOriginRequestProcessor implements Processor {
    private final ResourceSharingPolicy resourceSharingPolicy;

    public static Processor simpleCrossOriginRequestProcessor(final ResourceSharingPolicy resourceSharingPolicy) {
        Validators.validateNotNull(resourceSharingPolicy, "resourceSharingPolicy");
        return new SimpleCrossOriginRequestProcessor(resourceSharingPolicy);
    }

    @Override
    public void apply(final MetaData metaData) {
        // 1
        load(metaData).ifPresent(origin -> {
            // 2
            if(!resourceSharingPolicy.validateOrigin(origin)) {
                return;
            }

            // 3
            final ResponseHeaders responseHeaders = metaData.get(RESPONSE_HEADERS);
            responseHeaders.setHeader(ACCESS_CONTROL_ALLOW_ORIGIN, origin.internalValueForMapping());
            if (resourceSharingPolicy.supportsCredentials()) {
                responseHeaders.setHeader(ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
            }

            // 4
            resourceSharingPolicy.exposedHeaders().generateHeaderValue()
                    .ifPresent(value -> responseHeaders.setHeader(ACCESS_CONTROL_EXPOSE_HEADERS, value));
        });
    }
}
