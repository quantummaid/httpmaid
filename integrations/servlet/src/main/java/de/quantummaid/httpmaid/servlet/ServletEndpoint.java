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

package de.quantummaid.httpmaid.servlet;

import de.quantummaid.httpmaid.HttpMaid;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static de.quantummaid.httpmaid.util.Validators.validateNotNull;

public class ServletEndpoint extends HttpServlet {
    private static final long serialVersionUID = 0L;

    private final transient HttpMaid httpMaid;

    public ServletEndpoint(final HttpMaid httpMaid) {
        validateNotNull(httpMaid, "httpMaid");
        this.httpMaid = httpMaid;
    }

    public static ServletEndpoint servletEndpointFor(final HttpMaid httpMaid) {
        return new ServletEndpoint(httpMaid);
    }

    @Override
    protected void doGet(final HttpServletRequest request,
                         final HttpServletResponse response) throws IOException {
        ServletHandling.handle(httpMaid, request, response);
    }

    @Override
    protected void doPost(final HttpServletRequest request,
                          final HttpServletResponse response) throws IOException {
        ServletHandling.handle(httpMaid, request, response);
    }

    @Override
    protected void doPut(final HttpServletRequest request,
                         final HttpServletResponse response) throws IOException {
        ServletHandling.handle(httpMaid, request, response);
    }

    @Override
    protected void doDelete(final HttpServletRequest request,
                            final HttpServletResponse response) throws IOException {
        ServletHandling.handle(httpMaid, request, response);
    }

    @Override
    protected void doOptions(final HttpServletRequest request,
                             final HttpServletResponse response) throws IOException {
        ServletHandling.handle(httpMaid, request, response);
    }
}
