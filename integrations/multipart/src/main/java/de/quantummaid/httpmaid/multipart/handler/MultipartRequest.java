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

package de.quantummaid.httpmaid.multipart.handler;

import de.quantummaid.httpmaid.handler.http.HttpRequest;
import de.quantummaid.httpmaid.multipart.MultipartIteratorBody;
import lombok.*;

import static de.quantummaid.httpmaid.multipart.MultipartChainKeys.MULTIPART_ITERATOR_BODY;
import static de.quantummaid.httpmaid.util.Validators.validateNotNull;

@Getter
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class MultipartRequest {
    private final HttpRequest request;

    static MultipartRequest multipartRequest(final HttpRequest request) {
        validateNotNull(request, "request");
        return new MultipartRequest(request);
    }

    public HttpRequest httpRequest() {
        return this.request;
    }

    public MultipartIteratorBody partIterator() {
        return request.getMetaData().get(MULTIPART_ITERATOR_BODY);
    }
}
