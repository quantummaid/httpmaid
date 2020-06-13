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
import de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation.CloudFormationHandler;
import de.quantummaid.httpmaid.remotespecs.lambda.aws.httpapi.HttpApiInformation;
import de.quantummaid.httpmaid.remotespecs.lambda.aws.restapi.RestApiInformation;
import de.quantummaid.httpmaid.remotespecs.lambda.aws.websocketapi.WebsocketApiInformation;
import de.quantummaid.httpmaid.tests.givenwhenthen.deploy.Deployment;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.model.GetCallerIdentityResponse;

import java.io.File;
import java.util.Map;
import java.util.Optional;

import static de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation.CloudFormationHandler.connectToCloudFormation;
import static de.quantummaid.httpmaid.remotespecs.lambda.aws.httpapi.HttpApiHandler.loadHttpApiInformation;
import static de.quantummaid.httpmaid.remotespecs.lambda.aws.restapi.RestApiHandler.loadRestApiInformation;
import static de.quantummaid.httpmaid.remotespecs.lambda.aws.s3.S3Handler.deleteAllObjectsInBucket;
import static de.quantummaid.httpmaid.remotespecs.lambda.aws.s3.S3Handler.uploadToS3Bucket;
import static de.quantummaid.httpmaid.remotespecs.lambda.aws.websocketapi.WebsocketApiHandler.loadWebsocketApiInformation;
import static de.quantummaid.httpmaid.tests.givenwhenthen.deploy.Deployment.httpDeployment;
import static java.util.Optional.ofNullable;

/**
 * When stackIdentifier is not user-supplied (developerMode == false), then
 *  - the default stackIdentifier is: httpmaid-remotespecs-${AWS::AccountId}
 *  - the default stack naming scheme is: ${stackIdentifier}-(lambda|bucket)
 *  - The bucket is owned by ??? (whoever runs through the test setup first)
 *  - Any other user needing access must have that access granted some other way
 *    (additional policy, etc...). Users who cannot do that simply define
 *    a user-supplied stackidentifier.
 *  - cleanup policy
 *   - bucket stack remains
 *   - lambda stack is deleted
 *
 * When stackIdentifier is user-supplied (developerMode == true), we're in
 * the "per-user-infra" mode.
 *  - $stackIdentifier-lambda is created, owned by the user running the test
 *  - $stackIdentifier-bucket is created, ditto
 *  - cleanup policy
 *   - bucket stack remains
 *   - lambda stack remains
 *   (ie Both must be cleaned up / managed by the user)
 */
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class LambdaDeployer implements RemoteSpecsDeployer {
    private static final String SHARED_STACK_PREFIX = "httpmaid-remotespecs";
    private static final String RELATIVE_PATH_TO_LAMBDA_JAR = "/tests/lambda/target/remotespecs.jar";
    private static final String BUCKET_NAME = "remotespecs";
    private static final String REALTIVE_PATH_TO_CLOUDFORMATION_TEMPLATE = "/tests/remotespecs";
    private static final String REST_API_NAME = " RemoteSpecs Rest Api Lambda Proxy";
    private static final String HTTP_V2_PAYLOAD_API_NAME = " RemoteSpecs HTTP Api (Payload Version 2.0) Lambda Proxy";
    private static final String WEBSOCKET_API_NAME = " RemoteSpecs WebSockets Lambda Proxy";

    private final String stackIdentifier;
    private final Boolean developerMode;

    public static LambdaDeployer lambdaDeployer() {
        final LambdaDeployer deployer = userProvidedStackIdentifier()
                .map(LambdaDeployer::perUserLambdaDeployer)
                .orElseGet(LambdaDeployer::sharedLambdaDeployer);
        return deployer;
    }

    private static LambdaDeployer perUserLambdaDeployer(final String stackIdentifier) {
        return new LambdaDeployer(stackIdentifier, true);
    }

    private static LambdaDeployer sharedLambdaDeployer() {
        return new LambdaDeployer(sharedStackIdentifier(), false);
    }

    private static String sharedStackIdentifier() {
        final GetCallerIdentityResponse callerIdentity = StsClient.create().getCallerIdentity();
        final String accountId = callerIdentity.account();
        return String.format("%s-%s", SHARED_STACK_PREFIX, accountId);
    }

    @Override
    public RemoteSpecsDeployment deploy() {
        cleanUp();
        final String artifactBucketName = stackIdentifier + "-bucket"; // compute this

        create("cf-bucket.yml", stackIdentifier + "-bucket",
                Map.of("StackIdentifier", stackIdentifier,
                        "ArtifactBucketName", artifactBucketName));

        final String basePath = BaseDirectoryFinder.findProjectBaseDirectory();
        final String lambdaPath = basePath + RELATIVE_PATH_TO_LAMBDA_JAR;
        final File file = new File(lambdaPath);
        uploadToS3Bucket(artifactBucketName, stackIdentifier, file);

        create("cf-lambda.yml", stackIdentifier + "-lambda",
                Map.of("StackIdentifier", stackIdentifier,
                    "ArtifactBucketName", artifactBucketName,
                "ArtifactKey", stackIdentifier));

        final WebsocketApiInformation websocketApiInformation =
                loadWebsocketApiInformation(stackIdentifier + WEBSOCKET_API_NAME);
        final RestApiInformation restApiInformation =
                loadRestApiInformation(stackIdentifier + REST_API_NAME, websocketApiInformation.region());
        final HttpApiInformation httpApiInformation = loadHttpApiInformation(stackIdentifier + HTTP_V2_PAYLOAD_API_NAME);

        final Deployment restApiDeployment = httpDeployment(
                restApiInformation.baseUrl(),
                websocketApiInformation.baseUrl());
        final Deployment httpApiDeployment = httpDeployment(
                httpApiInformation.baseUrl(),
                websocketApiInformation.baseUrl()
        );

        return RemoteSpecsDeployment.remoteSpecsDeployment(this::cleanUp,
                Map.of(
                        LambdaRestApiRemoteSpecs.class, restApiDeployment,
                        LambdaHttpApiV2PayloadRemoteSpecs.class, httpApiDeployment
                )
        );
    }

    private void cleanUp() {
        if (developerMode) {
            return;
        }
        deleteAllObjectsInBucket(BUCKET_NAME);
        try (final CloudFormationHandler cloudFormationHandler = connectToCloudFormation()) {
            cloudFormationHandler.deleteStacksStartingWith(SHARED_STACK_PREFIX);
        }
    }

    public static void main(String[] args) {
        final LambdaDeployer lambdaDeployer = lambdaDeployer();
        System.out.println("first");
        lambdaDeployer.deploy();
        System.out.println("second");
        lambdaDeployer.deploy();
    }

    private static Optional<String> userProvidedStackIdentifier() {
        final String stackIdentifier = System.getenv("REMOTESPECS_STACK_IDENTIFIER");
        return ofNullable(stackIdentifier).map(s -> "dev-" + s);
    }

    private static void create(
            final String templateFilename,
            final String stackName,
            final Map<String, String> stackParameters) {

        final String basePath = BaseDirectoryFinder.findProjectBaseDirectory();
        final String templatePath = basePath + REALTIVE_PATH_TO_CLOUDFORMATION_TEMPLATE + "/" + templateFilename;
        try (final CloudFormationHandler cloudFormationHandler = connectToCloudFormation()) {
            cloudFormationHandler.createOrUpdateStack(stackName, templatePath, stackParameters);
        }
    }
}
