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

import de.quantummaid.httpmaid.handler.Handler;
import de.quantummaid.httpmaid.handler.http.HttpResponse;
import de.quantummaid.httpmaid.multipart.MultipartIteratorBody;
import de.quantummaid.httpmaid.multipart.handler.MultipartHandler;
import de.quantummaid.httpmaid.multipart.handler.MultipartRequest;

import java.util.StringJoiner;

public final class DumpMultipartBodyHandler implements MultipartHandler {

    public static Handler dumpMultipartBodyHandler() {
        return new DumpMultipartBodyHandler();
    }

    @Override
    public void handle(final MultipartRequest request, final HttpResponse response) {
        final MultipartIteratorBody multipartIteratorBody = request.partIterator();
        final String dump = dumpMultipart(multipartIteratorBody);
        response.setBody(dump);
    }

    private static String dumpMultipart(final MultipartIteratorBody body) {
        final StringJoiner joiner = new StringJoiner(", ", "[", "]");
        body.forEachRemaining(part -> {
            final StringBuilder builder = new StringBuilder();
            builder.append("{controlname=");
            builder.append(part.getControlName());
            part.getFileName().ifPresent(fileName -> {
                builder.append(",filename=");
                builder.append(fileName);
            });
            builder.append(",content=");
            builder.append(part.readContentToString());
            builder.append("}");
            joiner.add(builder.toString());
        });
        return joiner.toString();
    }
}
