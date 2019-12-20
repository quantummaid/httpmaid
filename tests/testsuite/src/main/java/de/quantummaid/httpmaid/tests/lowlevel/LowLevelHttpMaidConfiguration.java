/*
 * Copyright (c) 2019 Richard Hauswald - https://quantummaid.de/.
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

package de.quantummaid.httpmaid.tests.lowlevel;

import de.quantummaid.httpmaid.tests.lowlevel.handlers.ContentTypeInResponseHandler;
import de.quantummaid.httpmaid.tests.lowlevel.handlers.EchoContentTypeHandler;
import de.quantummaid.httpmaid.tests.lowlevel.handlers.LogHandler;
import de.quantummaid.httpmaid.HttpMaid;
import de.quantummaid.httpmaid.logger.LoggerImplementation;

import static de.quantummaid.httpmaid.tests.lowlevel.handlers.EchoBodyHandler.echoBodyHandler;
import static de.quantummaid.httpmaid.tests.lowlevel.handlers.ExceptionThrowingHandler.exceptionThrowingHandler;
import static de.quantummaid.httpmaid.tests.lowlevel.handlers.HeadersInResponseHandler.headersInResponseHandler;
import static de.quantummaid.httpmaid.tests.lowlevel.handlers.MyDownloadHandler.downloadHandler;
import static de.quantummaid.httpmaid.HttpMaid.anHttpMaid;
import static de.quantummaid.httpmaid.debug.DebugConfigurator.toBeInDebugMode;
import static de.quantummaid.httpmaid.http.HttpRequestMethod.*;
import static de.quantummaid.httpmaid.logger.LoggerConfigurators.toLogUsing;

public final class LowLevelHttpMaidConfiguration {
    public static StringBuilder logger;

    private LowLevelHttpMaidConfiguration() {
    }

    public static HttpMaid theLowLevelHttpMaidInstanceUsedForTesting() {
        logger = new StringBuilder();
        return anHttpMaid()
                .serving(echoBodyHandler())
                .forRequestPath("/echo").andRequestMethods(GET, POST, PUT, DELETE)
                .get("echo_contenttype", EchoContentTypeHandler.echoContentTypeHandler())
                .get("/set_contenttype_in_response", ContentTypeInResponseHandler.contentTypeInResponseHandler())
                .get("/headers_response", headersInResponseHandler())
                .get("/log", LogHandler.logHandler())
                .get("/download", downloadHandler())
                .get("/exception", exceptionThrowingHandler())
                .configured(toLogUsing(logger()))
                .configured(toBeInDebugMode())
                .build();
    }

    private static LoggerImplementation logger() {
        return logMessage -> {
            final String formattedMessage = logMessage.formattedMessage();
            logger.append(formattedMessage);
        };
    }
}
