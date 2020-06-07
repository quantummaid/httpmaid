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

package de.quantummaid.httpmaid.handler.http;

import de.quantummaid.httpmaid.chains.MetaData;
import de.quantummaid.httpmaid.handler.http.files.FileLoader;
import de.quantummaid.httpmaid.handler.http.files.MultiformatPath;
import de.quantummaid.httpmaid.http.headers.ContentType;
import de.quantummaid.httpmaid.http.headers.cookies.CookieBuilder;
import de.quantummaid.httpmaid.path.Path;
import de.quantummaid.httpmaid.util.Validators;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.io.File;
import java.io.InputStream;
import java.util.Map;

import static de.quantummaid.httpmaid.HttpMaidChainKeys.*;
import static de.quantummaid.httpmaid.http.Http.Headers.*;
import static de.quantummaid.httpmaid.http.Http.StatusCodes.FOUND;
import static de.quantummaid.httpmaid.http.headers.ContentType.fromString;
import static de.quantummaid.httpmaid.http.headers.cookies.CookieBuilder.cookie;
import static java.lang.String.format;
import static java.time.Instant.EPOCH;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class HttpResponse {
    private final MetaData metaData;

    public static HttpResponse httpResponse(final MetaData metaData) {
        Validators.validateNotNull(metaData, "metaData");
        return new HttpResponse(metaData);
    }

    public void addHeader(final String key, final String value) {
        metaData.get(RESPONSE_HEADERS).put(key, value);
    }

    public void setCookie(final CookieBuilder cookie) {
        Validators.validateNotNull(cookie, "cookie");
        addHeader(SET_COOKIE, cookie.build());
    }

    public void setCookie(final String name, final String value) {
        setCookie(cookie(name, value));
    }

    public void invalidateCookie(final String name) {
        setCookie(cookie(name, "").withExpiration(EPOCH));
    }

    public void setContentType(final String contentType) {
        setContentType(fromString(contentType));
    }

    public void setContentType(final ContentType contentType) {
        Validators.validateNotNull(contentType, "contentType");
        addHeader(CONTENT_TYPE, contentType.valueWithComment());
    }

    public void asDownloadWithFilename(final String filename) {
        Validators.validateNotNull(filename, "filename");
        final String contentDispositionHeader = format("attachment; filename=\"%s\"", filename);
        addHeader(CONTENT_DISPOSITION, contentDispositionHeader);
        setContentType("application/x-msdownload");
    }

    public void redirectTo(final String target) {
        Validators.validateNotNull(target, "target");
        setStatus(FOUND);
        addHeader(LOCATION, target);
    }

    public void setStatus(final int status) {
        metaData.set(RESPONSE_STATUS, status);
    }

    public void setBody(final Map<String, Object> map) {
        metaData.set(RESPONSE_BODY_OBJECT, map);
    }

    public void setBody(final String body) {
        Validators.validateNotNull(body, "body");
        metaData.set(RESPONSE_BODY_STRING, body);
    }

    public void setBody(final InputStream inputStream) {
        Validators.validateNotNull(inputStream, "inputStream");
        metaData.set(RESPONSE_STREAM, inputStream);
    }

    public void setFileAsBody(final String path) {
        Validators.validateNotNullNorEmpty(path, "path");
        final File file = new File(path);
        setFileAsBody(file);
    }

    public void setFileAsBody(final File file) {
        Validators.validateNotNull(file, "file");
        final InputStream stream = FileLoader.loadFile(file);
        setBody(stream);
    }

    public void setJavaResourceAsBody(final String path) {
        Validators.validateNotNullNorEmpty(path, "path");
        final MultiformatPath multiformatPath = MultiformatPath.multiformatPath(path);
        final InputStream stream = FileLoader.loadJavaResource(multiformatPath);
        setBody(stream);
    }

    public void mapPathToFileInDirectory(final String directory) {
        mapPathToFileInDirectory(directory, "");
    }

    public void mapPathToFileInDirectory(final String directory, final String pathPrefix) {
        final String filePath = rebasedRequestPath(directory, pathPrefix);
        setFileAsBody(filePath);
    }

    public void mapPathToJavaResourceInDirectory(final String directory) {
        mapPathToJavaResourceInDirectory(directory, "");
    }

    public void mapPathToJavaResourceInDirectory(final String directory, final String pathPrefix) {
        final String absolutePath = rebasedRequestPath(directory, pathPrefix);
        setJavaResourceAsBody(absolutePath);
    }

    private String rebasedRequestPath(final String directory, final String prefix) {
        final Path requestPath = metaData.get(PATH);
        final Path rebasedPath = requestPath
                .cutPrefix(prefix)
                .safelyRebaseTo(directory);
        return rebasedPath.raw();
    }
}
