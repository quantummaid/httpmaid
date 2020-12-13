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
import de.quantummaid.httpmaid.remotespecs.lambda.aws.apigateway.HttpApiInformation;
import de.quantummaid.httpmaid.remotespecs.lambda.aws.apigateway.WebsocketApiInformation;
import de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation.synthesizer.CloudformationModule;
import de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation.synthesizer.Namespace;
import de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation.synthesizer.modules.FunctionModule;
import de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation.synthesizer.modules.HttpApiModule;
import de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation.synthesizer.modules.WebsocketApiModule;
import de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation.synthesizer.modules.WebsocketRegistryModule;
import de.quantummaid.httpmaid.tests.givenwhenthen.deploy.Deployment;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Map;

import static de.quantummaid.httpmaid.remotespecs.lambda.aws.LambdaDeployer.lambdaDeployer;
import static de.quantummaid.httpmaid.remotespecs.lambda.aws.apigateway.HttpApiInformation.httpApiInformation;
import static de.quantummaid.httpmaid.remotespecs.lambda.aws.apigateway.WebsocketApiInformation.websocketApiInformation;
import static de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation.synthesizer.IntrinsicFunctions.sub;
import static de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation.synthesizer.PseudoParameters.REGION;
import static de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation.synthesizer.modules.FunctionModule.unauthorizedFunctionModule;
import static de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation.synthesizer.modules.HttpApiModule.unauthorizedHttpApiWithV1PayloadModule;
import static de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation.synthesizer.modules.WebsocketApiModule.websocketApiModule;
import static de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation.synthesizer.modules.WebsocketRegistryModule.websocketRegistryModule;
import static de.quantummaid.httpmaid.remotespecs.lambda.aws.dynamodb.DynamoDbHandler.resetTable;
import static de.quantummaid.httpmaid.tests.givenwhenthen.deploy.Deployment.httpDeployment;

@ExtendWith(RemoteSpecsExtension.class)
public final class UnauthHttpApiV1PayloadRemoteSpecs implements RemoteSpecs {

    public static CloudformationModule infrastructureRequirements(final Namespace namespace,
                                                                  final String bucketName,
                                                                  final String artifactKey) {
        return builder -> {
            final WebsocketRegistryModule websocketRegistryModule = websocketRegistryModule(namespace);
            final FunctionModule functionModule = unauthorizedFunctionModule(namespace, bucketName, artifactKey, Map.of(
                    "WEBSOCKET_REGISTRY_TABLE", websocketRegistryModule.dynamoDb().reference(),
                    "REGION", sub(REGION)
            ));

            final HttpApiModule httpApi = unauthorizedHttpApiWithV1PayloadModule(
                    namespace,
                    functionModule.function()
            );
            final WebsocketApiModule websocketApiModule = websocketApiModule(
                    namespace,
                    functionModule.role(),
                    functionModule.function(),
                    false
            );

            builder.withModule(websocketRegistryModule);
            builder.withModule(functionModule);
            builder.withModule(httpApi);
            builder.withModule(websocketApiModule);
        };
    }

    public static Deployment loadDeployment(final Map<String, String> stackOutputs,
                                            final Namespace namespace) {
        final String websocketRegistryDynamoDb = stackOutputs.get(namespace.id("WebsocketRegistryDynamoDb"));
        resetTable(websocketRegistryDynamoDb);

        final String region = stackOutputs.get("Region");
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
}
