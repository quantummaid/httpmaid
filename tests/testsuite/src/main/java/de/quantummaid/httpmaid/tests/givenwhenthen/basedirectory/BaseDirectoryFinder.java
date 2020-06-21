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

public final class BaseDirectoryFinder {
    private static final String PROJECT_ROOT_ANCHOR_FILENAME = ".projectrootanchor";
    private static final String PROJECT_ROOT_DIRECTORY = computeProjectBaseDirectory();

    private BaseDirectoryFinder() {
    }

    public static String findProjectBaseDirectory() {
        return PROJECT_ROOT_DIRECTORY;
    }

    private static String computeProjectBaseDirectory() {
        final String location = BaseDirectoryFinder.class.getProtectionDomain().getCodeSource().getLocation().getFile();

        File currentDirectory = new File(location);
        while (!anchorFileIn(currentDirectory).exists()) {
            if (isRootDirectory(currentDirectory)) {
                throw new BaseDirectoryNotFoundException(location);
            }
            currentDirectory = parentOf(currentDirectory);
        }

        return currentDirectory.getAbsolutePath();
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
