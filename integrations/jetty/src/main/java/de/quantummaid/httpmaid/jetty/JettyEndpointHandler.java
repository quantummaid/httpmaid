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
import de.quantummaid.httpmaid.http.Headers;
import de.quantummaid.httpmaid.http.HeadersBuilder;
import de.quantummaid.httpmaid.http.QueryParameters;
import de.quantummaid.httpmaid.http.QueryParametersBuilder;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;

import static de.quantummaid.httpmaid.endpoint.RawHttpRequest.rawHttpRequestBuilder;
import static de.quantummaid.httpmaid.http.HeadersBuilder.headersBuilder;
import static de.quantummaid.httpmaid.util.Validators.validateNotNull;
import static java.util.Arrays.asList;

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
                    final Headers headers = extractHeaders(request);
                    builder.withHeaders(headers);
                    final QueryParameters queryParameters = extractQueryParameters(request);
                    builder.withQueryParameters(queryParameters);
                    final InputStream body = request.getInputStream();
                    builder.withBody(body);
                    return builder.build();
                },
                response -> {
                    response.setHeaders(httpServletResponse::addHeader);
                    httpServletResponse.setStatus(response.status());
                    final OutputStream outputStream = httpServletResponse.getOutputStream();
                    response.streamBodyToOutputStream(outputStream);
                });
    }

    private static Headers extractHeaders(final HttpServletRequest request) {
        final HeadersBuilder headersBuilder = headersBuilder();
        final Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            final String headerName = headerNames.nextElement();
            final Enumeration<String> values = request.getHeaders(headerName);
            while (values.hasMoreElements()) {
                final String value = values.nextElement();
                headersBuilder.withAdditionalHeader(headerName, value);
            }
        }
        return headersBuilder.build();
    }

    private static QueryParameters extractQueryParameters(final HttpServletRequest request) {
        final Enumeration<String> parameterNames = request.getParameterNames();
        final QueryParametersBuilder builder = QueryParameters.builder();
        while (parameterNames.hasMoreElements()) {
            final String parameterName = parameterNames.nextElement();
            final String[] parameterValues = request.getParameterValues(parameterName);
            builder.withParameter(parameterName, asList(parameterValues));
        }
        return builder.build();
    }
}
