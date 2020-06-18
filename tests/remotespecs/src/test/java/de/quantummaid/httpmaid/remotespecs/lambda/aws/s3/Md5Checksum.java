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

package de.quantummaid.httpmaid.remotespecs.lambda.aws.s3;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import static de.quantummaid.httpmaid.util.Validators.validateNotNullNorEmpty;
import static java.lang.String.format;

/**
 * Source: https://stackoverflow.com/questions/304268/getting-a-files-md5-checksum-in-java
 */
@ToString
@EqualsAndHashCode
public class Md5Checksum {
    public static final String MD5_STRING_PATTERN = "[0-9a-fA-F]{32}";
    private static final Predicate<String> VALID_MD5_STRING = Pattern.compile(MD5_STRING_PATTERN).asMatchPredicate();

    private final String value;

    Md5Checksum(final String value) {
        validateNotNullNorEmpty(value, "Md5Checksum");
        if (!VALID_MD5_STRING.test(value)) {
            throw new IllegalArgumentException(
                    format("md5 checksum '%s' must match pattern '%s'", value, MD5_STRING_PATTERN));
        }
        this.value = value.toLowerCase();
    }

    public String getValue() {
        return value;
    }

    public static Md5Checksum ofFile(final File file) {
        final byte[] b = md5ChecksumBytesOf(file.getAbsolutePath());
        final StringBuilder result = new StringBuilder();

        for (int i = 0; i < b.length; i++) {
            result.append(Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1));
        }
        return new Md5Checksum(result.toString());
    }

    private static byte[] md5ChecksumBytesOf(final String filename) {
        try (InputStream fis = new FileInputStream(filename)) {
            final byte[] buffer = new byte[1024];
            final MessageDigest complete = MessageDigest.getInstance("MD5");
            int numRead;

            do {
                numRead = fis.read(buffer);
                if (numRead > 0) {
                    complete.update(buffer, 0, numRead);
                }
            } while (numRead != -1);
            return complete.digest();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
