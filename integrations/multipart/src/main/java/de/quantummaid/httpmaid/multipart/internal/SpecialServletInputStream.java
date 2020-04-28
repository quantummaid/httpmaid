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

package de.quantummaid.httpmaid.multipart.internal;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import java.io.IOException;
import java.io.InputStream;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class SpecialServletInputStream extends ServletInputStream {
    private static final int READ_LIMIT = -1;
    private final InputStream inputStream;

    public static ServletInputStream servletInputStreamBackedBy(final InputStream inputStream) {
        return new SpecialServletInputStream(inputStream);
    }

    @Override
    public int read() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int read(final byte[] b, final int off, final int len) throws IOException {
        if (READ_LIMIT > 0) {
            return this.inputStream.read(b, off, Math.min(READ_LIMIT, len));
        }
        return this.inputStream.read(b, off, len);
    }

    @Override
    public boolean isFinished() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isReady() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setReadListener(final ReadListener arg0) {
        throw new UnsupportedOperationException();
    }
}
