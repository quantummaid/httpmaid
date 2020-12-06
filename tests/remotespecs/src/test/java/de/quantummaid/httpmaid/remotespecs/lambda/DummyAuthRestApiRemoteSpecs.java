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

import de.quantummaid.httpmaid.remotespecs.RemoteSpecs;
import de.quantummaid.httpmaid.remotespecs.RemoteSpecsDeployer;
import de.quantummaid.httpmaid.remotespecs.RemoteSpecsExtension;
import de.quantummaid.httpmaid.remotespecs.lambda.aws.apigateway.RestApiInformation;
import de.quantummaid.httpmaid.remotespecs.lambda.aws.apigateway.WebsocketApiInformation;
import de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation.synthesizer.CloudformationModule;
import de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation.synthesizer.Namespace;
import de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation.synthesizer.modules.FunctionModule;
import de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation.synthesizer.modules.RestApiModule;
import de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation.synthesizer.modules.WebsocketApiModule;
import de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation.synthesizer.modules.WebsocketRegistryModule;
import de.quantummaid.httpmaid.tests.givenwhenthen.TestEnvironment;
import de.quantummaid.httpmaid.tests.givenwhenthen.deploy.Deployment;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Map;

import static de.quantummaid.httpmaid.remotespecs.lambda.aws.LambdaDeployer.lambdaDeployer;
import static de.quantummaid.httpmaid.remotespecs.lambda.aws.apigateway.RestApiInformation.restApiInformation;
import static de.quantummaid.httpmaid.remotespecs.lambda.aws.apigateway.WebsocketApiInformation.websocketApiInformation;
import static de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation.synthesizer.IntrinsicFunctions.sub;
import static de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation.synthesizer.PseudoParameters.REGION;
import static de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation.synthesizer.modules.FunctionModule.dummyAuthorizedFunctionModule;
import static de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation.synthesizer.modules.RestApiModule.restApiModule;
import static de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation.synthesizer.modules.WebsocketApiModule.websocketApiModule;
import static de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation.synthesizer.modules.WebsocketRegistryModule.websocketRegistryModule;
import static de.quantummaid.httpmaid.remotespecs.lambda.aws.dynamodb.DynamoDbHandler.resetTable;
import static de.quantummaid.httpmaid.tests.givenwhenthen.deploy.Deployment.httpDeployment;

@ExtendWith(RemoteSpecsExtension.class)
public final class DummyAuthRestApiRemoteSpecs implements RemoteSpecs {

    public static CloudformationModule infrastructureRequirements(final Namespace namespace,
                                                                  final String bucketName,
                                                                  final String artifactKey) {
        return builder -> {
            final WebsocketRegistryModule websocketRegistryModule = websocketRegistryModule(namespace);

            final FunctionModule functionModule = dummyAuthorizedFunctionModule(namespace, bucketName, artifactKey, Map.of(
                    "WEBSOCKET_REGISTRY_TABLE", websocketRegistryModule.dynamoDb().reference(),
                    "REGION", sub(REGION)
            ));

            final RestApiModule restApiModule = restApiModule(namespace, functionModule.function());
            final WebsocketApiModule websocketApiModule = websocketApiModule(
                    namespace,
                    functionModule.role(),
                    functionModule.function(),
                    true
            );

            builder.withModule(websocketRegistryModule);
            builder.withModule(functionModule);
            builder.withModule(restApiModule);
            builder.withModule(websocketApiModule);
        };
    }

    public static Deployment loadDeployment(final Map<String, String> stackOutputs,
                                            final Namespace namespace) {
        final String websocketRegistryDynamoDb = stackOutputs.get(namespace.id("WebsocketRegistryDynamoDb"));
        resetTable(websocketRegistryDynamoDb);

        final String region = stackOutputs.get("Region");
        final String restWebsocketApiId = stackOutputs.get(namespace.id("WebsocketApiId"));
        final WebsocketApiInformation websocketApiInformation = websocketApiInformation(restWebsocketApiId, region);

        final String restApiId = stackOutputs.get(namespace.id("RestApiId"));
        final RestApiInformation apiInformation = restApiInformation(restApiId, region);

        return httpDeployment(
                apiInformation.baseUrl(),
                websocketApiInformation.baseUrl()
        );
    }

    @Override
    public RemoteSpecsDeployer provideDeployer() {
        return lambdaDeployer();
    }

    @Test
    public void dummyAuthorizerCanEnrichContext(final TestEnvironment testEnvironment) {
        testEnvironment.givenTheStaticallyDeployedTestInstance()
                .when().aWebsocketIsConnected()
                .andWhen().aWebsocketMessageIsSent("{ \"message\": \"returnLambdaContext\" }")
                .allWebsocketsHaveReceivedTheMessage("bar");
    }
}
