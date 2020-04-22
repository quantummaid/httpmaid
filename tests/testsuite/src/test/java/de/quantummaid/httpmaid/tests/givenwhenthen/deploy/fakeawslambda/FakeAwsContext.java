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

package de.quantummaid.httpmaid.tests.givenwhenthen.deploy.fakeawslambda;

import com.amazonaws.services.lambda.runtime.ClientContext;
import com.amazonaws.services.lambda.runtime.CognitoIdentity;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import static de.quantummaid.httpmaid.tests.givenwhenthen.deploy.fakeawslambda.FakeLambdaLogger.fakeLambdaLogger;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class FakeAwsContext implements Context {

    public static Context fakeAwsContext() {
        return new FakeAwsContext();
    }

    @Override
    public String getAwsRequestId() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getLogGroupName() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getLogStreamName() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getFunctionName() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getFunctionVersion() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getInvokedFunctionArn() {
        throw new UnsupportedOperationException();
    }

    @Override
    public CognitoIdentity getIdentity() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ClientContext getClientContext() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getRemainingTimeInMillis() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getMemoryLimitInMB() {
        throw new UnsupportedOperationException();
    }

    @Override
    public LambdaLogger getLogger() {
        return fakeLambdaLogger();
    }
}
