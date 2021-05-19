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

import de.quantummaid.httpmaid.client.HttpMaidClientException;
import de.quantummaid.httpmaid.remotespecs.RemoteSpecs;
import de.quantummaid.httpmaid.remotespecs.RemoteSpecsDeployer;
import de.quantummaid.httpmaid.remotespecs.RemoteSpecsExtension;
import de.quantummaid.httpmaid.remotespecs.lambda.aws.Artifact;
import de.quantummaid.httpmaid.remotespecs.lambda.aws.Artifacts;
import de.quantummaid.httpmaid.remotespecs.lambda.aws.apigateway.HttpApiInformation;
import de.quantummaid.httpmaid.remotespecs.lambda.aws.apigateway.WebsocketApiInformation;
import de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation.synthesizer.CloudformationModule;
import de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation.synthesizer.CloudformationResource;
import de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation.synthesizer.Namespace;
import de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation.synthesizer.StackOutputs;
import de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation.synthesizer.modules.*;
import de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation.synthesizer.resources.Cognito;
import de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudwatch.CloudwatchLogGroupReference;
import de.quantummaid.httpmaid.tests.givenwhenthen.TestEnvironment;
import de.quantummaid.httpmaid.tests.givenwhenthen.WebsocketTestClientConnectException;
import de.quantummaid.httpmaid.tests.givenwhenthen.deploy.Deployment;
import org.eclipse.jetty.websocket.api.UpgradeException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static de.quantummaid.httpmaid.remotespecs.lambda.aws.LambdaDeployer.lambdaDeployer;
import static de.quantummaid.httpmaid.remotespecs.lambda.aws.LambdaDeployer.loadToken;
import static de.quantummaid.httpmaid.remotespecs.lambda.aws.apigateway.HttpApiInformation.httpApiInformation;
import static de.quantummaid.httpmaid.remotespecs.lambda.aws.apigateway.WebsocketApiInformation.websocketApiInformation;
import static de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation.synthesizer.CloudformationName.cloudformationName;
import static de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation.synthesizer.CloudformationOutput.cloudformationOutput;
import static de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation.synthesizer.IntrinsicFunctions.sub;
import static de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation.synthesizer.PseudoParameters.REGION;
import static de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation.synthesizer.modules.CognitoModule.cognitoModule;
import static de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation.synthesizer.modules.FunctionModule.cognitoAuthorizedFunctionModule;
import static de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation.synthesizer.modules.HttpApiModule.authorizedHttpApiWithV2PayloadModule;
import static de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation.synthesizer.modules.WebsocketApiModule.websocketApiModule;
import static de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation.synthesizer.modules.WebsocketRegistryModule.websocketRegistryModule;
import static de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudwatch.CloudwatchLogGroupReference.builder;
import static de.quantummaid.httpmaid.remotespecs.lambda.aws.dynamodb.DynamoDbHandler.resetTable;
import static de.quantummaid.httpmaid.tests.givenwhenthen.deploy.Deployment.httpDeployment;
import static java.util.Optional.ofNullable;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

@ExtendWith(RemoteSpecsExtension.class)
public final class CognitoHttpApiV2PayloadRemoteSpecs implements RemoteSpecs {
    private static String accessToken;
    private static String maliciousAccessToken;
    private static String tokenFromDifferentCognitoClient;
    private static CloudwatchLogGroupReference logGroupReference;
    private static String websocketRegistryDynamoDb;

    @Override
    public Optional<String> additionInformationOnError() {
        return ofNullable(logGroupReference)
                .map(CloudwatchLogGroupReference::buildDescription);
    }

    public static CloudformationModule infrastructureRequirements(final Namespace namespace,
                                                                  final Artifacts artifacts) {
        return builder -> {
            final CognitoModule cognitoModule = cognitoModule(namespace);
            final Namespace malicious2 = namespace.sub("malicious2");
            final CloudformationResource otherClient = Cognito.poolClient(
                    malicious2.id("OtherClient"),
                    cognitoModule.pool()
            );
            builder
                    .withResources(otherClient)
                    .withOutputs(cloudformationOutput(malicious2.id("PoolId"), cognitoModule.pool().reference()))
                    .withOutputs(cloudformationOutput(malicious2.id("PoolClientId"), otherClient.reference()));
            final WebsocketRegistryModule websocketRegistryModule = websocketRegistryModule(namespace);
            final Artifact artifact = artifacts.jarImage();
            final FunctionModule functionModule = cognitoAuthorizedFunctionModule(namespace, artifact, Map.of(
                    "WEBSOCKET_REGISTRY_TABLE", websocketRegistryModule.dynamoDb().reference(),
                    "POOL_ID", cognitoModule.pool().reference(),
                    "POOL_CLIENT_ID", cognitoModule.poolClient().reference(),
                    "REGION", sub(REGION)
            ));
            final String functionName = functionModule.function().name().asId();
            logGroupReference = builder()
                    .withConventionalLogGroupNameForLambda(functionName)
                    .withRegionFromEnvironment()
                    .build();
            final HttpApiModule httpApi = authorizedHttpApiWithV2PayloadModule(
                    namespace, functionModule.function(), cognitoModule.pool(), cognitoModule.poolClient());
            final WebsocketApiModule websocketApiModule = websocketApiModule(
                    namespace, functionModule.role(), functionModule.function(), true);
            final Namespace malicious = namespace.sub("malicious");
            final CognitoModule maliciousCognitoModule = cognitoModule(malicious);
            builder.withModule(cognitoModule);
            builder.withModule(maliciousCognitoModule);
            builder.withModule(websocketRegistryModule);
            builder.withModule(functionModule);
            builder.withModule(httpApi);
            builder.withModule(websocketApiModule);
        };
    }

    public static Deployment loadDeployment(final StackOutputs stackOutputs,
                                            final Namespace namespace) {
        accessToken = loadToken(stackOutputs, namespace);
        maliciousAccessToken = loadToken(stackOutputs, namespace.sub("malicious"));
        tokenFromDifferentCognitoClient = loadToken(stackOutputs, namespace.sub("malicious2"));

        websocketRegistryDynamoDb = stackOutputs.get(namespace.id("WebsocketRegistryDynamoDb"));
        resetTable(websocketRegistryDynamoDb);

        final String region = stackOutputs.get(cloudformationName("Region"));
        final String websocketApiId = stackOutputs.get(namespace.id("WebsocketApiId"));
        final WebsocketApiInformation websocketApiInformation = websocketApiInformation(websocketApiId, region);

        final String apiId = stackOutputs.get(namespace.id("HttpApi"));
        final HttpApiInformation apiInformation = httpApiInformation(apiId, region);

        return httpDeployment(
                apiInformation.baseUrl(),
                websocketApiInformation.baseUrl()
        );
    }

    @Override
    public RemoteSpecsDeployer provideDeployer() {
        return lambdaDeployer();
    }

    @Override
    public Optional<String> accessToken() {
        return Optional.of(accessToken);
    }

    @Test
    public void cognitoAuthorizerCanEnrichContext(final TestEnvironment testEnvironment) {
        testEnvironment.givenTheStaticallyDeployedTestInstance()
                .when().aWebsocketIsConnected(mapWithAccessToken(), Map.of())
                .andWhen().aWebsocketMessageIsSent("{ \"message\": \"returnLambdaContext\" }")
                .allWebsocketsHaveReceivedTheMessage("bar");
    }

    @Test
    public void httpRequestsWithoutTokenAreRejected(final TestEnvironment testEnvironment) {
        testEnvironment.givenTheStaticallyDeployedTestInstance()
                .when().aRequestToThePath("/").viaThePostMethod().withAnEmptyBody().isIssued()
                .theStatusCodeWas(401)
                .theResponseBodyWas("{\"message\":\"Unauthorized\"}");
    }

    @Test
    public void httpRequestsWithBrokenTokenAreRejected(final TestEnvironment testEnvironment) {
        testEnvironment.givenTheStaticallyDeployedTestInstance()
                .when().aRequestToThePath("/").viaThePostMethod().withAnEmptyBody()
                .withTheHeader("Authorization", "Bearer abcdef").isIssued()
                .theStatusCodeWas(401)
                .theResponseBodyWas("{\"message\":\"Unauthorized\"}");
    }

    @Test
    public void httpRequestsWithTokenFromDifferentCognitoAreRejected(final TestEnvironment testEnvironment) {
        testEnvironment.givenTheStaticallyDeployedTestInstance()
                .when().aRequestToThePath("/").viaThePostMethod().withAnEmptyBody()
                .withTheHeader("Authorization", String.format("Bearer %s", maliciousAccessToken)).isIssued()
                .theStatusCodeWas(401)
                .theResponseBodyWas("{\"message\":\"Unauthorized\"}");
    }

    @Test
    public void httpRequestsWithTokenFromDifferentCognitoClientAreRejected(final TestEnvironment testEnvironment) {
        testEnvironment.givenTheStaticallyDeployedTestInstance()
                .when().aRequestToThePath("/").viaThePostMethod().withAnEmptyBody()
                .withTheHeader("Authorization", String.format("Bearer %s", tokenFromDifferentCognitoClient)).isIssued()
                .theStatusCodeWas(401)
                .theResponseBodyWas("{\"message\":\"Unauthorized\"}");
    }

    @Test
    public void websocketsWithoutTokenAreRejected(final TestEnvironment testEnvironment) {
        Exception exception = null;
        try {
            testEnvironment.givenTheStaticallyDeployedTestInstance()
                    .when().aWebsocketIsConnected(Map.of(), Map.of());
        } catch (final WebsocketTestClientConnectException e) {
            exception = e;
        }
        assertThat(exception, is(notNullValue()));
        final Throwable cause1 = exception.getCause();
        assertThat(cause1, instanceOf(HttpMaidClientException.class));
        final Throwable cause2 = cause1.getCause();
        assertThat(cause2, instanceOf(HttpMaidClientException.class));
        final Throwable cause3 = cause2.getCause();
        assertThat(cause3, instanceOf(UpgradeException.class));
        assertThat(cause3.getMessage(), is("Failed to upgrade to websocket: " +
                "Unexpected HTTP Response Status Code: 500 Internal Server Error"));
    }

    @Test
    public void websocketsWithBrokenTokenAreRejected(final TestEnvironment testEnvironment) {
        Exception exception = null;
        try {
            testEnvironment.givenTheStaticallyDeployedTestInstance()
                    .when().aWebsocketIsConnected(Map.of("access_token", List.of("abcdef")), Map.of());
        } catch (final WebsocketTestClientConnectException e) {
            exception = e;
        }
        assertThat(exception, is(notNullValue()));
        final Throwable cause1 = exception.getCause();
        assertThat(cause1, instanceOf(HttpMaidClientException.class));
        final Throwable cause2 = cause1.getCause();
        assertThat(cause2, instanceOf(HttpMaidClientException.class));
        final Throwable cause3 = cause2.getCause();
        assertThat(cause3, instanceOf(UpgradeException.class));
        assertThat(cause3.getMessage(), is("Failed to upgrade to websocket: " +
                "Unexpected HTTP Response Status Code: 500 Internal Server Error"));
    }

    @Test
    public void websocketsWithTokenFromDifferentCognitoAreRejected(final TestEnvironment testEnvironment) {
        Exception exception = null;
        try {
            testEnvironment.givenTheStaticallyDeployedTestInstance()
                    .when().aWebsocketIsConnected(Map.of("access_token", List.of(maliciousAccessToken)), Map.of());
        } catch (final WebsocketTestClientConnectException e) {
            exception = e;
        }
        assertThat(exception, is(notNullValue()));
        final Throwable cause1 = exception.getCause();
        assertThat(cause1, instanceOf(HttpMaidClientException.class));
        final Throwable cause2 = cause1.getCause();
        assertThat(cause2, instanceOf(HttpMaidClientException.class));
        final Throwable cause3 = cause2.getCause();
        assertThat(cause3, instanceOf(UpgradeException.class));
        assertThat(cause3.getMessage(), is("Failed to upgrade to websocket: " +
                "Unexpected HTTP Response Status Code: 403 Forbidden"));
    }

    @Test
    public void websocketsWithTokenFromDifferentCognitoClientAreRejected(final TestEnvironment testEnvironment) {
        Exception exception = null;
        try {
            testEnvironment.givenTheStaticallyDeployedTestInstance()
                    .when().aWebsocketIsConnected(Map.of("access_token", List.of(tokenFromDifferentCognitoClient)), Map.of());
        } catch (final WebsocketTestClientConnectException e) {
            exception = e;
        }
        assertThat(exception, is(notNullValue()));
        final Throwable cause1 = exception.getCause();
        assertThat(cause1, instanceOf(HttpMaidClientException.class));
        final Throwable cause2 = cause1.getCause();
        assertThat(cause2, instanceOf(HttpMaidClientException.class));
        final Throwable cause3 = cause2.getCause();
        assertThat(cause3, instanceOf(UpgradeException.class));
        assertThat(cause3.getMessage(), is("Failed to upgrade to websocket: " +
                "Unexpected HTTP Response Status Code: 403 Forbidden"));
    }

    @Test
    public void noLeakedConnectionsInWebsocketRegistryAfterDisconnectByClient(final TestEnvironment testEnvironment) {
        Shared.noLeakedConnectionsInWebsocketRegistryAfterDisconnectByClient(
                testEnvironment, websocketRegistryDynamoDb, mapWithAccessToken());
    }

    @Test
    public void noLeakedConnectionsInWebsocketRegistryAfterDisconnectByServer(final TestEnvironment testEnvironment) {
        Shared.noLeakedConnectionsInWebsocketRegistryAfterDisconnectByServer(
                testEnvironment, websocketRegistryDynamoDb, mapWithAccessToken());
    }
}
