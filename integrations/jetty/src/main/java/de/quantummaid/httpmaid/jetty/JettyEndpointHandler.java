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
import de.quantummaid.httpmaid.endpoint.RawHttpRequestBuilder;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.quantummaid.httpmaid.endpoint.RawHttpRequest.rawHttpRequestBuilder;
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
                       final HttpServletResponse httpServletResponse) {
        httpMaid.handleRequest(() -> {
                    final RawHttpRequestBuilder builder = rawHttpRequestBuilder();
                    final String path = request.getPathInfo();
                    builder.withPath(path);
                    final String method = request.getMethod();
                    builder.withMethod(method);
                    final Map<String, List<String>> headers = extractHeaders(request);
                    builder.withHeaders(headers);
                    final Map<String, String> queryParameters = extractQueryParameters(request);
                    builder.withUniqueQueryParameters(queryParameters);
                    final InputStream body = request.getInputStream();
                    builder.withBody(body);
                    return builder.build();
                },
                response -> {
                    response.setHeaders(httpServletResponse::setHeader);
                    httpServletResponse.setStatus(response.status());
                    final OutputStream outputStream = httpServletResponse.getOutputStream();
                    response.streamBodyToOutputStream(outputStream);
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
