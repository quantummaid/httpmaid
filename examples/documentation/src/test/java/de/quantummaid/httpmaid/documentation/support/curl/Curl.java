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

package de.quantummaid.httpmaid.documentation.support.curl;

import de.quantummaid.httpmaid.client.HttpClientRequestBuilder;
import de.quantummaid.httpmaid.client.SimpleHttpResponseObject;
import de.quantummaid.httpmaid.documentation.support.curl.elements.CurlElement;
import de.quantummaid.httpmaid.documentation.support.curl.tokens.BashTokens;
import de.quantummaid.httpmaid.documentation.support.curl.tokens.TokenStream;

import java.util.List;

import static de.quantummaid.httpmaid.documentation.support.curl.CurlCommandBuilder.curlCommandBuilder;
import static de.quantummaid.httpmaid.documentation.support.curl.ResourceLoader.resourceToString;
import static de.quantummaid.httpmaid.documentation.support.curl.elements.DataCurlElement.dataCurlElement;
import static de.quantummaid.httpmaid.documentation.support.curl.elements.HeaderCurlElement.headerCurlElement;
import static de.quantummaid.httpmaid.documentation.support.curl.elements.RequestCurlElement.requestCurlElement;
import static de.quantummaid.httpmaid.documentation.support.curl.elements.UrlCurlElement.urlCurlElement;
import static de.quantummaid.httpmaid.documentation.support.curl.elements.VerboseElement.verboseElement;
import static java.lang.String.format;

public final class Curl {
    private static final List<CurlElement> CURL_ELEMENTS = List.of(
            urlCurlElement(),
            headerCurlElement(),
            dataCurlElement(),
            requestCurlElement(),
            verboseElement()
    );

    public static void main(String[] args) {
        parseFromCurlFile("body.curl");
    }

    public static HttpClientRequestBuilder<SimpleHttpResponseObject> parseFromCurlFile(final String resourcePath) {
        final String fileContent = resourceToString(resourcePath);
        final TokenStream tokenStream = BashTokens.bashTokensIn(fileContent);

        final String prompt = tokenStream.next();
        if (!prompt.equals("$")) {
            throw new IllegalArgumentException(format("Commands need to start with \"$\" but found '%s' in resource file '%s'", fileContent, resourcePath));
        }

        final String command = tokenStream.next();
        if (!command.equals("curl")) {
            throw new IllegalArgumentException(format("Command is not 'curl' in command '%s' in resource file '%s'", fileContent, resourcePath));
        }

        final CurlCommandBuilder commandBuilder = curlCommandBuilder();
        while (tokenStream.hasNext()) {
            multiplex(tokenStream, commandBuilder);
        }
        return commandBuilder.build();
    }

    private static void multiplex(final TokenStream tokenStream, final CurlCommandBuilder commandBuilder) {
        final String token = tokenStream.next();
        final CurlElement element = CURL_ELEMENTS.stream()
                .filter(curlElement -> curlElement.match(token))
                .findFirst()
                .orElseThrow(() -> new UnsupportedOperationException(format("Unsupported curl option '%s'", token)));
        element.act(token, tokenStream, commandBuilder);
    }
}
