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

package de.quantummaid.httpmaid.multipart;

import de.quantummaid.httpmaid.HttpMaidChainKeys;
import de.quantummaid.httpmaid.chains.ChainExtender;
import de.quantummaid.httpmaid.chains.ChainModule;
import de.quantummaid.httpmaid.chains.ChainName;
import de.quantummaid.httpmaid.http.Http;
import de.quantummaid.httpmaid.http.headers.ContentType;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import static de.quantummaid.httpmaid.HttpMaidChains.*;
import static de.quantummaid.httpmaid.chains.ChainName.chainName;
import static de.quantummaid.httpmaid.chains.rules.Jump.jumpTo;
import static de.quantummaid.httpmaid.http.headers.ContentType.fromString;
import static java.lang.String.format;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class MultipartModule implements ChainModule {
    private static final ChainName PROCESS_BODY_MULTIPART = chainName("PROCESS_BODY_MULTIPART");
    private static final ContentType CONTENT_TYPE = fromString("multipart/form-data");
    private static final String RULE_DESCRIPTION = format("%s=%s", Http.Headers.CONTENT_TYPE,
            CONTENT_TYPE.internalValueForMapping());

    public static MultipartModule multipartModule() {
        return new MultipartModule();
    }

    @Override
    public void register(final ChainExtender extender) {
        extender.createChain(PROCESS_BODY_MULTIPART, jumpTo(DETERMINE_HANDLER), jumpTo(EXCEPTION_OCCURRED));
        extender.appendProcessor(PROCESS_BODY_MULTIPART, MultipartProcessor.multipartProcessor());

        extender.routeIf(PROCESS_BODY, jumpTo(PROCESS_BODY_MULTIPART), HttpMaidChainKeys.REQUEST_CONTENT_TYPE,
                contentType -> contentType.equals(CONTENT_TYPE), RULE_DESCRIPTION);
    }
}
