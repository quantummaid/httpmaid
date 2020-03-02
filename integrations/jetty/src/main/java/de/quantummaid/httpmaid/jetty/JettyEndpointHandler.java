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

package de.quantummaid.httpmaid.jetty;

import de.quantummaid.httpmaid.HttpMaid;
import de.quantummaid.httpmaid.chains.MetaData;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.quantummaid.httpmaid.HttpMaidChainKeys.*;
import static de.quantummaid.httpmaid.chains.MetaData.emptyMetaData;
import static de.quantummaid.httpmaid.util.Streams.streamInputStreamToOutputStream;
import static de.quantummaid.httpmaid.util.Streams.stringToInputStream;
import static de.quantummaid.httpmaid.util.Validators.validateNotNull;
import static java.util.Collections.singletonList;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
final class JettyEndpointHandler extends AbstractHandler {
    private final HttpMaid httpMaid;

    static AbstractHandler jettyEndpointHandler(final HttpMaid httpMaid) {
        validateNotNull(httpMaid, "httpMaid");
        return new JettyEndpointHandler(httpMaid);
    }

    @Override
    public void handle(final String target,
                       final Request request,
                       final HttpServletRequest httpServletRequest,
                       final HttpServletResponse httpServletResponse) throws IOException {
        final String method = request.getMethod();
        final String path = request.getPathInfo();
        final Map<String, List<String>> headers = extractHeaders(request);
        final Map<String, String> queryParameters = extractQueryParameters(request);
        final InputStream body = request.getInputStream();

        final MetaData metaData = emptyMetaData();
        metaData.set(RAW_REQUEST_HEADERS, headers);
        metaData.set(RAW_REQUEST_QUERY_PARAMETERS, queryParameters);
        metaData.set(RAW_METHOD, method);
        metaData.set(RAW_PATH, path);
        metaData.set(REQUEST_BODY_STREAM, body);
        metaData.set(IS_HTTP_REQUEST, true);

        httpMaid.handleRequest(metaData, httpResponse -> {
            final Map<String, String> responseHeaders = metaData.get(RESPONSE_HEADERS);
            responseHeaders.forEach(httpServletResponse::setHeader);
            final int responseStatus = metaData.get(RESPONSE_STATUS);
            httpServletResponse.setStatus(responseStatus);
            final OutputStream outputStream = httpServletResponse.getOutputStream();
            final InputStream inputStream = metaData.getOptional(RESPONSE_STREAM).orElseGet(() -> stringToInputStream(""));
            streamInputStreamToOutputStream(inputStream, outputStream);
        });
    }

    private static Map<String, List<String>> extractHeaders(final HttpServletRequest request) {
        final Enumeration<String> headerNames = request.getHeaderNames();
        final Map<String, List<String>> headers = new HashMap<>();
        while (headerNames.hasMoreElements()) {
            final String headerName = headerNames.nextElement();
            final String value = request.getHeader(headerName);
            headers.put(headerName, singletonList(value));
        }
        return headers;
    }

    private static Map<String, String> extractQueryParameters(final HttpServletRequest request) {
        final Enumeration<String> parameterNames = request.getParameterNames();
        final Map<String, String> queryParameters = new HashMap<>();
        while (parameterNames.hasMoreElements()) {
            final String parameterName = parameterNames.nextElement();
            final String value = request.getParameter(parameterName);
            queryParameters.put(parameterName, value);
        }
        return queryParameters;
    }
}
