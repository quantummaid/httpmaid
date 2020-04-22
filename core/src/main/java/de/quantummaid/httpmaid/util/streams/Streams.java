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

package de.quantummaid.httpmaid.util.streams;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Scanner;

import static de.quantummaid.httpmaid.util.streams.StreamProcessingException.streamProcessingException;
import static java.nio.charset.StandardCharsets.UTF_8;

public final class Streams {
    private static final int BUFFER_SIZE = 1024;

    private Streams() {
    }

    public static InputStream stringToInputStream(final String string) {
        return new ByteArrayInputStream(string.getBytes(UTF_8));
    }

    public static String inputStreamToString(final InputStream inputStream) {
        final Scanner scanner = new Scanner(inputStream, UTF_8).useDelimiter("\\A");
        if (scanner.hasNext()) {
            return scanner.next();
        }
        return "";
    }

    public static void streamInputStreamToOutputStream(final InputStream inputStream,
                                                       final OutputStream outputStream) {
        try {
            final byte[] buffer = new byte[BUFFER_SIZE];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, length);
            }
            inputStream.close();
            outputStream.close();
        } catch (final IOException e) {
            throw streamProcessingException(e);
        }
    }
}
