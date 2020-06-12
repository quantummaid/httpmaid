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

package de.quantummaid.httpmaid.remotespecs.lambda;

import de.quantummaid.httpmaid.remotespecs.BaseDirectoryFinder;
import de.quantummaid.httpmaid.remotespecs.RemoteSpecsDeployer;
import de.quantummaid.httpmaid.remotespecs.RemoteSpecsDeployment;
import de.quantummaid.httpmaid.remotespecs.lambda.aws.restapi.RestApiInformation;
import de.quantummaid.httpmaid.remotespecs.lambda.aws.websocketapi.WebsocketApiInformation;
import de.quantummaid.httpmaid.tests.givenwhenthen.deploy.Deployment;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.io.File;
import java.util.Map;

import static de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation.CloudFormationHandler.createStack;
import static de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation.CloudFormationHandler.deleteStacksStartingWith;
import static de.quantummaid.httpmaid.remotespecs.lambda.aws.restapi.RestApiHandler.loadRestApiInformation;
import static de.quantummaid.httpmaid.remotespecs.lambda.aws.s3.S3Handler.deleteAllObjectsInBucket;
import static de.quantummaid.httpmaid.remotespecs.lambda.aws.s3.S3Handler.uploadToS3Bucket;
import static de.quantummaid.httpmaid.remotespecs.lambda.aws.websocketapi.WebsocketApiHandler.loadWebsocketApiInformation;
import static java.util.UUID.randomUUID;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class LambdaDeployer implements RemoteSpecsDeployer {
    private static final String PREFIX = "remotespecsX";
    private static final String RELATIVE_PATH_TO_LAMBDA_JAR = "/tests/lambda/target/remotespecs.jar";
    private static final String BUCKET_NAME = "remotespecs";
    private static final String REALTIVE_PATH_TO_CLOUDFORMATION_TEMPLATE = "/tests/remotespecs/cloudformation.yaml";
    private static final String REST_API_NAME = " RemoteSpecs HTTP Lambda Proxy";
    private static final String WEBSOCKET_API_NAME = " RemoteSpecs WebSockets Lambda Proxy";

    private static final int PORT = 443;
    private final String stackIdentifier;

    public static LambdaDeployer lambdaDeployer() {
        final String stackIdentifier = PREFIX + randomUUID().toString();
        return new LambdaDeployer(stackIdentifier);
    }

    @Override
    public RemoteSpecsDeployment deploy() {
        cleanUp();
        create(stackIdentifier);

        final WebsocketApiInformation websocketApiInformation =
                loadWebsocketApiInformation(stackIdentifier + WEBSOCKET_API_NAME);
        final RestApiInformation restApiInformation =
                loadRestApiInformation(stackIdentifier + REST_API_NAME, websocketApiInformation.region());

        final Deployment restApiDeployment = Deployment.httpDeployment(
                restApiInformation.baseUrl(), websocketApiInformation.baseUrl());

        return RemoteSpecsDeployment.remoteSpecsDeployment(this::cleanUp,
                Map.of(LambdaRestApiRemoteSpecs.class, restApiDeployment));
    }

    private void cleanUp() {
        deleteAllObjectsInBucket(BUCKET_NAME);
        deleteStacksStartingWith(PREFIX);
    }

    private static void create(final String stackIdentifier) {
        final String basePath = BaseDirectoryFinder.findProjectBaseDirectory();
        final String lambdaPath = basePath + RELATIVE_PATH_TO_LAMBDA_JAR;
        final File file = new File(lambdaPath);
        uploadToS3Bucket(BUCKET_NAME, stackIdentifier, file);
        final String templatePath = basePath + REALTIVE_PATH_TO_CLOUDFORMATION_TEMPLATE;
        createStack(stackIdentifier, templatePath);
    }
}
