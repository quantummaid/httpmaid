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

package de.quantummaid.httpmaid.handler.http.files;

import static de.quantummaid.httpmaid.util.Validators.validateNotNull;

public final class FileDoesNotExistException extends RuntimeException {

    private FileDoesNotExistException(final String message, final Throwable cause) {
        super(message, cause);
    }

    private static FileDoesNotExistException fileDoesNotExistException(final String path,
                                                                       final String type,
                                                                       final Throwable cause) {
        validateNotNull(path, "path");
        final String workingDirectory = System.getProperty("user.dir");
        final String message = String.format("Could not find %s at %s (current working directory: %s", type, path, workingDirectory);
        return new FileDoesNotExistException(message, cause);
    }

    static FileDoesNotExistException javaResourceDoesNotExistException(final MultiformatPath path,
                                                                       final Throwable cause) {
        validateNotNull(path, "path");
        final String formatted = path.formatted("", "");
        return fileDoesNotExistException(formatted, "java resource", cause);
    }

    static FileDoesNotExistException javaResourceDoesNotExistException(final MultiformatPath path) {
        return javaResourceDoesNotExistException(path, null);
    }

    static FileDoesNotExistException filesystemFileDoesNotExistException(final String path,
                                                                         final Throwable cause) {
        return fileDoesNotExistException(path, "file", cause);
    }
}
