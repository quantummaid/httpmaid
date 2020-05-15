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

import de.quantummaid.httpmaid.HttpMaid;
import de.quantummaid.httpmaid.remotespecs.BaseDirectoryFinder;
import de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation.CloudFormationHandler;
import de.quantummaid.httpmaid.remotespecs.lambda.aws.restapi.RestApiInformation;
import de.quantummaid.httpmaid.remotespecs.lambda.aws.websocketapi.WebsocketApiInformation;
import de.quantummaid.httpmaid.tests.givenwhenthen.client.ClientFactory;
import de.quantummaid.httpmaid.tests.givenwhenthen.deploy.Deployer;
import de.quantummaid.httpmaid.tests.givenwhenthen.deploy.Deployment;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.io.File;
import java.util.List;

import static de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation.CloudFormationHandler.createStack;
import static de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation.CloudFormationHandler.deleteStack;
import static de.quantummaid.httpmaid.remotespecs.lambda.aws.restapi.RestApiHandler.loadRestApiInformation;
import static de.quantummaid.httpmaid.remotespecs.lambda.aws.s3.S3Handler.uploadToS3Bucket;
import static de.quantummaid.httpmaid.remotespecs.lambda.aws.websocketapi.WebsocketApiHandler.loadWebsocketApiInformation;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class LambdaDeployer implements Deployer {
    private static final String STACK_IDENTIFIER = "foo";
    private static final String RELATIVE_PATH_TO_LAMBDA_JAR = "/tests/lambda/target/remotespecs.jar";
    private static final String BUCKET_NAME = "remotespecs";
    private static final String S3_JAR_NAME = "jar";
    private static final String REALTIVE_PATH_TO_CLOUDFORMATION_TEMPLATE = "/tests/remotespecs/cloudformation.yaml";
    private static final String REST_API_NAME = "RemoteSpecs HTTP Lambda Proxy";
    private static final String WEBSOCKET_API_NAME = "RemoteSpecs WebSockets Lambda Proxy";
    private static final int WAIT_TIME = 60_000;

    private static final int PORT = 443;

    public static LambdaDeployer lambdaDeployer() {
        return new LambdaDeployer();
    }

    @Override
    public Deployment deploy(final HttpMaid httpMaid) {
        deleteStack(STACK_IDENTIFIER);
        create();
        final RestApiInformation restApiInformation = loadRestApiInformation(REST_API_NAME);
        final WebsocketApiInformation websocketApiInformation = loadWebsocketApiInformation(WEBSOCKET_API_NAME);
        final String region = websocketApiInformation.region();
        final String httpHost = restApiInformation.host(region);
        final String httpBasePath = restApiInformation.basePath();
        final String websocketHost = websocketApiInformation.host();
        final String websocketBasePath = websocketApiInformation.basePath();

        try {
            Thread.sleep(WAIT_TIME);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return Deployment.httpsDeploymentWithBasePath(httpHost, websocketHost, PORT, httpBasePath, websocketBasePath);
    }

    @Override
    public void cleanUp() {
        CloudFormationHandler.deleteStack(STACK_IDENTIFIER);
    }

    @Override
    public List<ClientFactory> supportedClients() {
        throw new UnsupportedOperationException();
    }

    private static void create() {
        final String basePath = BaseDirectoryFinder.findProjectBaseDirectory();
        final String lambdaPath = basePath + RELATIVE_PATH_TO_LAMBDA_JAR;
        final File file = new File(lambdaPath);
        uploadToS3Bucket(BUCKET_NAME, S3_JAR_NAME, file);
        final String templatePath = basePath + REALTIVE_PATH_TO_CLOUDFORMATION_TEMPLATE;
        createStack(STACK_IDENTIFIER, templatePath);
    }
}
