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

package de.quantummaid.httpmaid.client.issuer.real;

import de.quantummaid.httpmaid.client.BasePath;
import de.quantummaid.httpmaid.client.HttpClientRequest;
import de.quantummaid.httpmaid.client.RawClientResponse;
import de.quantummaid.httpmaid.client.RequestPath;
import de.quantummaid.httpmaid.client.issuer.Issuer;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.apache.http.*;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.apache.http.protocol.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static de.quantummaid.httpmaid.client.HttpMaidClientException.httpMaidClientException;
import static de.quantummaid.httpmaid.client.RawClientResponse.rawClientResponse;
import static de.quantummaid.httpmaid.client.issuer.real.Endpoint.endpoint;
import static de.quantummaid.httpmaid.client.issuer.real.NormalConnectionFactory.normalConnectionFactory;
import static de.quantummaid.httpmaid.client.issuer.real.PooledConnectionFactory.pooledConnectionFactory;
import static java.util.Arrays.stream;
import static org.apache.http.protocol.HttpProcessorBuilder.create;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class RealIssuer implements Issuer {
    private final Endpoint endpoint;
    private final ConnectionFactory connectionFactory;

    public static Issuer realIssuer(final Protocol protocol,
                                    final String host,
                                    final int port) {
        return new RealIssuer(endpoint(protocol, host, port), normalConnectionFactory());
    }

    public static Issuer realIssuerWithConnectionReuse(final Protocol protocol,
                                                       final String host,
                                                       final int port) {
        return new RealIssuer(endpoint(protocol, host, port), pooledConnectionFactory());
    }

    @Override
    public <T> T issue(final HttpClientRequest<T> request,
                       final Function<RawClientResponse, T> responseMapper,
                       final BasePath basePath) {
        final RequestPath path = request.path(basePath);
        final String url = this.endpoint.toUrl(path);
        final String method = request.method();
        final HttpEntityEnclosingRequest lowLevelRequest = new BasicHttpEntityEnclosingRequest(method, url);
        request.headers().forEach(header -> lowLevelRequest.addHeader(header.name(), header.value()));
        request.body().ifPresent(requestBody -> {
            final InputStreamEntity entity = new InputStreamEntity(requestBody);
            lowLevelRequest.setEntity(entity);
        });
        try (Connection connection = connectionFactory.getConnectionTo(endpoint)) {
            final HttpProcessor httpProcessor = create()
                    .add(new RequestContent())
                    .add(new RequestTargetHost())
                    .build();
            final HttpCoreContext context = HttpCoreContext.create();
            context.setTargetHost(new HttpHost(this.endpoint.host()));
            httpProcessor.process(lowLevelRequest, context);
            final HttpRequestExecutor httpRequestExecutor = new HttpRequestExecutor();
            final HttpClientConnection connectionObject = connection.connectionObject();
            final HttpResponse response = httpRequestExecutor.execute(lowLevelRequest, connectionObject, context);
            final int statusCode = response.getStatusLine().getStatusCode();
            final Map<String, List<String>> headers = new HashMap<>();
            stream(response.getAllHeaders())
                    .forEach(header -> {
                        final String headerName = header.getName().toLowerCase();
                        final List<String> headerValues = headers.getOrDefault(headerName, new ArrayList<>());
                        headerValues.add(header.getValue());
                        headers.put(headerName, headerValues);
                    });
            final InputStream body = response.getEntity().getContent();
            final RawClientResponse rawClientResponse = rawClientResponse(statusCode, headers, body);
            return responseMapper.apply(rawClientResponse);
        } catch (final IOException | HttpException e) {
            throw httpMaidClientException(e);
        }
    }

    @Override
    public void close() {
        connectionFactory.close();
    }
}
