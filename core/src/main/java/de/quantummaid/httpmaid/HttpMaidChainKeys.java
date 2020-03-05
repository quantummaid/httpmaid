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

package de.quantummaid.httpmaid;

import de.quantummaid.httpmaid.chains.MetaDataKey;
import de.quantummaid.httpmaid.handler.Handler;
import de.quantummaid.httpmaid.http.Headers;
import de.quantummaid.httpmaid.http.HttpRequestMethod;
import de.quantummaid.httpmaid.http.PathParameters;
import de.quantummaid.httpmaid.http.QueryParameters;
import de.quantummaid.httpmaid.http.headers.ContentType;
import de.quantummaid.httpmaid.logger.Logger;
import de.quantummaid.httpmaid.path.Path;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

public final class HttpMaidChainKeys {

    private HttpMaidChainKeys() {
    }

    public static final MetaDataKey<Throwable> EXCEPTION = MetaDataKey.metaDataKey("EXCEPTION");

    public static final MetaDataKey<Object> AUTHENTICATION_INFORMATION = MetaDataKey.metaDataKey("AUTHENTICATION_INFORMATION");
    public static final MetaDataKey<Logger> LOGGER = MetaDataKey.metaDataKey("LOGGER");

    public static final MetaDataKey<Handler> HANDLER = MetaDataKey.metaDataKey("HANDLER");

    public static final MetaDataKey<Boolean> IS_HTTP_REQUEST = MetaDataKey.metaDataKey("IS_HTTP_REQUEST");

    public static final MetaDataKey<Map<String, List<String>>> RAW_REQUEST_HEADERS = MetaDataKey.metaDataKey("RAW_REQUEST_HEADERS");
    public static final MetaDataKey<Map<String, String>> RAW_REQUEST_QUERY_PARAMETERS =
            MetaDataKey.metaDataKey("RAW_REQUEST_QUERY_PARAMETERS");
    public static final MetaDataKey<String> RAW_METHOD = MetaDataKey.metaDataKey("RAW_METHOD");
    public static final MetaDataKey<String> RAW_PATH = MetaDataKey.metaDataKey("RAW_PATH");
    public static final MetaDataKey<Path> PATH = MetaDataKey.metaDataKey("PATH");
    public static final MetaDataKey<PathParameters> PATH_PARAMETERS = MetaDataKey.metaDataKey("PATH_PARAMETERS");
    public static final MetaDataKey<QueryParameters> QUERY_PARAMETERS = MetaDataKey.metaDataKey("QUERY_PARAMETERS");
    public static final MetaDataKey<HttpRequestMethod> METHOD = MetaDataKey.metaDataKey("METHOD");

    public static final MetaDataKey<InputStream> REQUEST_BODY_STREAM = MetaDataKey.metaDataKey("REQUEST_BODY_STREAM");
    public static final MetaDataKey<String> REQUEST_BODY_STRING = MetaDataKey.metaDataKey("REQUEST_BODY_STRING");
    public static final MetaDataKey<Map<String, Object>> REQUEST_BODY_MAP = MetaDataKey.metaDataKey("REQUEST_BODY_MAP");

    public static final MetaDataKey<ContentType> REQUEST_CONTENT_TYPE = MetaDataKey.metaDataKey("REQUEST_CONTENT_TYPE");
    public static final MetaDataKey<Headers> REQUEST_HEADERS = MetaDataKey.metaDataKey("REQUEST_HEADERS");

    public static final MetaDataKey<Object> RESPONSE_BODY_OBJECT = MetaDataKey.metaDataKey("RESPONSE_BODY_MAP");
    public static final MetaDataKey<String> RESPONSE_BODY_STRING = MetaDataKey.metaDataKey("RESPONSE_BODY_STRING");
    public static final MetaDataKey<InputStream> RESPONSE_STREAM = MetaDataKey.metaDataKey("RESPONSE_STREAM");

    public static final MetaDataKey<ContentType> RESPONSE_CONTENT_TYPE = MetaDataKey.metaDataKey("RESPONSE_CONTENT_TYPE");
    public static final MetaDataKey<Map<String, String>> RESPONSE_HEADERS = MetaDataKey.metaDataKey("RESPONSE_HEADERS");
    public static final MetaDataKey<Integer> RESPONSE_STATUS = MetaDataKey.metaDataKey("RESPONSE_STATUS");
}
