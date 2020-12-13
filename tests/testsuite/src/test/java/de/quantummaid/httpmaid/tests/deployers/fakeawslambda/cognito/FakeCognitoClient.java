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

package de.quantummaid.httpmaid.tests.deployers.fakeawslambda.cognito;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.GetUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.GetUserResponse;

import java.util.function.Consumer;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class FakeCognitoClient implements CognitoIdentityProviderClient {
    private final String username;

    public static CognitoIdentityProviderClient fakeCognitoClient(final String username) {
        return new FakeCognitoClient(username);
    }

    @Override
    public String serviceName() {
        return "fakeclient";
    }

    @Override
    public GetUserResponse getUser(final Consumer<GetUserRequest.Builder> getUserRequest)
            throws AwsServiceException, SdkClientException {
        final GetUserRequest.Builder builder = GetUserRequest.builder();
        getUserRequest.accept(builder);
        return GetUserResponse.builder()
                .username(username)
                .build();
    }

    @Override
    public void close() {
    }
}
