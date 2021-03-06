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

package de.quantummaid.httpmaid.tests.specs.multipart;

import de.quantummaid.httpmaid.tests.givenwhenthen.TestEnvironment;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static de.quantummaid.httpmaid.tests.givenwhenthen.MultipartBuilder.startingWith;
import static de.quantummaid.httpmaid.tests.givenwhenthen.TestEnvironments.ALL_ENVIRONMENTS;
import static de.quantummaid.httpmaid.tests.givenwhenthen.builders.MultipartElement.aFile;
import static de.quantummaid.httpmaid.tests.givenwhenthen.builders.MultipartElement.aFormControl;
import static de.quantummaid.httpmaid.tests.specs.multipart.handler.MultipartHttpMaidConfiguration.theMultipartHttpMaidInstanceUsedForTesting;

public final class MultipartSpecs {

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void testMultipartFileUploadWithGet(final TestEnvironment testEnvironment) {
        testEnvironment.given(theMultipartHttpMaidInstanceUsedForTesting())
                .when().aRequestToThePath("/dump").viaTheGetMethod()
                .withTheMultipartBody(startingWith(aFile("myfile", "asdf.txt", "foooo"))).isIssued()
                .theStatusCodeWas(200)
                .theResponseBodyWas("[{controlname=myfile,filename=asdf.txt,content=foooo}]");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void testMultipartFileUploadWithPost(final TestEnvironment testEnvironment) {
        testEnvironment.given(theMultipartHttpMaidInstanceUsedForTesting())
                .when().aRequestToThePath("/dump").viaThePostMethod()
                .withTheMultipartBody(startingWith(aFile("myfile", "asdf.txt", "foooo"))).isIssued()
                .theStatusCodeWas(200)
                .theResponseBodyWas("[{controlname=myfile,filename=asdf.txt,content=foooo}]");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void testMultipartFileUploadWithPut(final TestEnvironment testEnvironment) {
        testEnvironment.given(theMultipartHttpMaidInstanceUsedForTesting())
                .when().aRequestToThePath("/dump").viaThePutMethod()
                .withTheMultipartBody(startingWith(aFile("myfile", "asdf.txt", "foooo"))).isIssued()
                .theStatusCodeWas(200)
                .theResponseBodyWas("[{controlname=myfile,filename=asdf.txt,content=foooo}]");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void testMultipartFileUploadWithDelete(final TestEnvironment testEnvironment) {
        testEnvironment.given(theMultipartHttpMaidInstanceUsedForTesting())
                .when().aRequestToThePath("/dump").viaTheDeleteMethod()
                .withTheMultipartBody(startingWith(aFile("myfile", "asdf.txt", "foooo"))).isIssued()
                .theStatusCodeWas(200)
                .theResponseBodyWas("[{controlname=myfile,filename=asdf.txt,content=foooo}]");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void testMultipartWithoutFile(final TestEnvironment testEnvironment) {
        testEnvironment.given(theMultipartHttpMaidInstanceUsedForTesting())
                .when().aRequestToThePath("/dump").viaThePostMethod()
                .withTheMultipartBody(startingWith(aFormControl("foo", "bar"))).isIssued()
                .theStatusCodeWas(200)
                .theResponseBodyWas("[{controlname=foo,content=bar}]");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void testMultipartWithFieldsBeforeTheFileUpload(final TestEnvironment testEnvironment) {
        testEnvironment.given(theMultipartHttpMaidInstanceUsedForTesting())
                .when().aRequestToThePath("/dump").viaThePostMethod()
                .withTheMultipartBody(startingWith(aFormControl("control1", "content1"))
                        .followedBy(aFormControl("control2", "content2"))
                        .followedBy(aFile("myfile", "asdf.txt", "foooo"))).isIssued()
                .theStatusCodeWas(200)
                .theResponseBodyWas("[{controlname=control1,content=content1}, {controlname=control2,content=content2}, {controlname=myfile,filename=asdf.txt,content=foooo}]");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void testMultipartFieldsAfterTheFileUpload(final TestEnvironment testEnvironment) {
        testEnvironment.given(theMultipartHttpMaidInstanceUsedForTesting())
                .when().aRequestToThePath("/dump").viaThePostMethod()
                .withTheMultipartBody(startingWith(aFile("myfile", "asdf.txt", "foooooooo"))
                        .followedBy(aFormControl("ignoredcontrol1", "ignoredcontent1"))
                        .followedBy(aFormControl("ignoredcontrol2", "ignoredcontent2"))
                        .followedBy(aFile("myfile", "ignoredfile.txt", "ignoredfilecontent"))).isIssued()
                .theStatusCodeWas(200)
                .theResponseBodyWas("[{controlname=myfile,filename=asdf.txt,content=foooooooo}, {controlname=ignoredcontrol1,content=ignoredcontent1}, {controlname=ignoredcontrol2,content=ignoredcontent2}, {controlname=myfile,filename=ignoredfile.txt,content=ignoredfilecontent}]");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void testMultipartWithMultipleFiles(final TestEnvironment testEnvironment) {
        testEnvironment.given(theMultipartHttpMaidInstanceUsedForTesting())
                .when().aRequestToThePath("/dump").viaThePostMethod()
                .withTheMultipartBody(startingWith(aFile("file1", "file1.pdf", "content1"))
                        .followedBy(aFile("file2", "file2.pdf", "content2"))
                        .followedBy(aFile("file3", "file3.pdf", "content3"))).isIssued()
                .theStatusCodeWas(200)
                .theResponseBodyWas("[{controlname=file1,filename=file1.pdf,content=content1}, {controlname=file2,filename=file2.pdf,content=content2}, {controlname=file3,filename=file3.pdf,content=content3}]");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void testAuthenticationByFirstMultipartPart(final TestEnvironment testEnvironment) {
        testEnvironment.given(theMultipartHttpMaidInstanceUsedForTesting())
                .when().aRequestToThePath("/authenticated").viaThePostMethod()
                .withTheMultipartBody(startingWith(aFormControl("authentication", "username=bob"))
                        .followedBy(aFormControl("control1", "foo"))).isIssued()
                .theStatusCodeWas(200)
                .theResponseBodyWas("Authenticated as: bob");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void testWrongAuthorizationGetsRejectedBeforeProcessingFile(final TestEnvironment testEnvironment) {
        testEnvironment.given(theMultipartHttpMaidInstanceUsedForTesting())
                .when().aRequestToThePath("/authorized").viaThePostMethod().withTheMultipartBody(startingWith(aFormControl("authentication", "username=normaluser"))
                .followedBy(aFile("file", "file.txt", "content"))).isIssued()
                .theStatusCodeWas(403)
                .theResponseBodyWas("Access denied!");
    }

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void testAuthorizationBeforeProcessingFile(final TestEnvironment testEnvironment) {
        testEnvironment.given(theMultipartHttpMaidInstanceUsedForTesting())
                .when().aRequestToThePath("/authorized").viaThePostMethod().withTheMultipartBody(startingWith(aFormControl("authentication", "username=admin"))
                .followedBy(aFile("file", "file.txt", "content"))).isIssued()
                .theStatusCodeWas(200)
                .theResponseBodyWas("Welcome to the admin section!");
    }
}
