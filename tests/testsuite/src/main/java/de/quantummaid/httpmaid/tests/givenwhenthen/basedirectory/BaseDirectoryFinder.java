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

package de.quantummaid.httpmaid.tests.givenwhenthen.basedirectory;

import java.io.File;
import java.net.URL;
import java.util.Optional;

public final class BaseDirectoryFinder {
    private static final String PROJECT_ROOT_ANCHOR_FILENAME = ".projectrootanchor";
    private static final String PROJECT_ROOT_DIRECTORY = computeProjectBaseDirectory();

    private BaseDirectoryFinder() {
    }

    public static String findProjectBaseDirectory() {
        return PROJECT_ROOT_DIRECTORY;
    }

    private static String computeProjectBaseDirectory() {
        final URL codeSourceUrl = BaseDirectoryFinder.class.getProtectionDomain().getCodeSource().getLocation();
        final String codeSourceLocation = codeSourceUrl.getFile();
        final Optional<String> fromCodeSource = computeProjectBaseDirectoryFrom(codeSourceLocation);
        if (fromCodeSource.isPresent()) {
            return fromCodeSource.get();
        }
        final String currentDirectory = System.getProperty("user.dir");
        final Optional<String> fromCurrentDirectory = computeProjectBaseDirectoryFrom(currentDirectory);
        if (fromCurrentDirectory.isPresent()) {
            return fromCurrentDirectory.get();
        }

        throw new BaseDirectoryNotFoundException(
                String.format("unable to find project root directory (code source URL: %s, current working directory: %s)",
                        codeSourceUrl, currentDirectory));
    }

    private static Optional<String> computeProjectBaseDirectoryFrom(final String startDirectory) {

        File currentDirectory = new File(startDirectory);
        while (!anchorFileIn(currentDirectory).exists()) {
            if (isRootDirectory(currentDirectory)) {
                return Optional.empty();
            }
            currentDirectory = parentOf(currentDirectory);
        }

        return Optional.of(currentDirectory.getAbsolutePath());
    }

    private static File anchorFileIn(final File parent) {
        return new File(parent, PROJECT_ROOT_ANCHOR_FILENAME);
    }

    private static boolean isRootDirectory(final File f) {
        return f.getParent() == null;
    }

    private static File parentOf(final File directory) {
        return new File(directory.getParent());
    }
}
