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

package de.quantummaid.httpmaid.tests.specs.multipart.handler;

import de.quantummaid.httpmaid.HttpMaid;
import de.quantummaid.httpmaid.multipart.MultipartIteratorBody;
import de.quantummaid.httpmaid.multipart.MultipartPart;
import de.quantummaid.httpmaid.path.Path;

import static de.quantummaid.httpmaid.HttpMaid.anHttpMaid;
import static de.quantummaid.httpmaid.http.Http.StatusCodes.FORBIDDEN;
import static de.quantummaid.httpmaid.http.HttpRequestMethod.*;
import static de.quantummaid.httpmaid.multipart.MultipartChainKeys.MULTIPART_ITERATOR_BODY;
import static de.quantummaid.httpmaid.multipart.MultipartConfigurators.toExposeMultipartBodiesUsingMultipartIteratorBody;
import static de.quantummaid.httpmaid.security.SecurityConfigurators.toAuthenticateRequestsUsing;
import static de.quantummaid.httpmaid.security.SecurityConfigurators.toAuthorizeRequestsUsing;
import static de.quantummaid.httpmaid.tests.specs.multipart.handler.Util.extractUsername;
import static java.util.Optional.empty;

public final class MultipartHttpMaidConfiguration {

    private MultipartHttpMaidConfiguration() {
    }

    public static HttpMaid theMultipartHttpMaidInstanceUsedForTesting() {
        return anHttpMaid()
                .serving(DumpMultipartBodyHandler.dumpMultipartBodyHandler())
                .forRequestPath("/dump").andRequestMethods(GET, POST, PUT, DELETE)
                .serving(AuthenticatedHandler.authenticatedHandler())
                .forRequestPath("/authenticated").andRequestMethods(GET, POST, PUT, DELETE)
                .serving(AuthorizedHandler.authorizedHandler())
                .forRequestPath("/authorized").andRequestMethods(GET, POST, PUT, DELETE)
                .configured(toAuthenticateRequestsUsing(request -> {
                    final Path path = request.path();
                    if (path.matches("/authenticated") || path.matches("/authorized")) {
                        final MultipartIteratorBody multipartIteratorBody = request.getMetaData().get(MULTIPART_ITERATOR_BODY);
                        final MultipartPart firstPart = multipartIteratorBody.next("authentication");
                        final String content = firstPart.readContentToString();
                        return extractUsername(content);
                    }
                    return empty();
                }).afterBodyProcessing().notFailingOnMissingAuthentication())

                .configured(toAuthorizeRequestsUsing((authenticationInformation, request) -> {
                    if (request.path().matches("/authorized")) {
                        return authenticationInformation
                                .map("admin"::equals)
                                .orElse(false);
                    } else {
                        return true;
                    }
                }).afterBodyProcessing()
                        .rejectingUnauthorizedRequestsUsing((request, response) -> {
                            response.setStatus(FORBIDDEN);
                            response.setBody("Access denied!");
                        })
                )
                .configured(toExposeMultipartBodiesUsingMultipartIteratorBody())
                .build();
    }
}
