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

package de.quantummaid.httpmaid.tests;


import de.quantummaid.httpmaid.tests.givenwhenthen.Given;
import org.junit.jupiter.api.Test;

import static de.quantummaid.httpmaid.client.HttpClientRequest.aPostRequestToThePath;

public final class ClientSpecs {
    private static final String charactersThatNeedEncoding = "[]{}\"§#";

    @Test
    public void clientDoesNotAppendATrailingSlashToPathToDirectory() {
        Given.givenAnHttpServer()
                .when().aRequestIsMadeToThePath("/qwer")
                .theServerReceivedARequestToThePath("/qwer");
    }

    @Test
    public void clientDoesNotAppendATrailingSlashToPathToFile() {
        Given.givenAnHttpServer()
                .when().aRequestIsMadeToThePath("/qwer/tweet.json")
                .theServerReceivedARequestToThePath("/qwer/tweet.json");
    }

    @Test
    public void clientKeepsAnAppendedTrailingSlashInPath() {
        Given.givenAnHttpServer()
                .when().aRequestIsMadeToThePath("/qwer/")
                .theServerReceivedARequestToThePath("/qwer/");
    }

    @Test
    public void clientEncodesPath() {
        Given.givenAnHttpServer()
                .when().aRequestIsMadeToThePath("/" + charactersThatNeedEncoding)
                .theServerReceivedARequestToThePath("/%5B%5D%7B%7D%22%C2%A7%23");
    }

    @Test
    public void emptyBodyValuesWithMapMaidDoNotCauseProblems() {
        Given.givenAnHttpServer()
                .when().aRequestIsMade(aPostRequestToThePath("/test"))
                .theServerReceivedARequestToThePath("/test");
    }
}
