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

package de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation.synthesizer.modules;

import de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation.synthesizer.CloudformationModule;
import de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation.synthesizer.CloudformationResource;
import de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation.synthesizer.CloudformationTemplateBuilder;
import de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation.synthesizer.Namespace;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import static de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation.synthesizer.CloudformationOutput.cloudformationOutput;
import static de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation.synthesizer.resources.ApiGatewayV2.*;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class HttpApiModule implements CloudformationModule {
    private static final String PAYLOAD_V1 = "1.0";
    private static final String PAYLOAD_V2 = "2.0";

    private final Namespace namespace;
    private final CloudformationResource function;
    private final String payloadVersion;
    private final boolean authorized;
    private final CloudformationResource pool;
    private final CloudformationResource poolClient;

    public static HttpApiModule authorizedHttpApiWithV2PayloadModule(final Namespace namespace,
                                                                     final CloudformationResource function,
                                                                     final CloudformationResource pool,
                                                                     final CloudformationResource poolClient) {
        return new HttpApiModule(namespace, function, PAYLOAD_V2, true, pool, poolClient);
    }

    public static HttpApiModule unauthorizedHttpApiWithV2PayloadModule(final Namespace namespace,
                                                                       final CloudformationResource function) {
        return new HttpApiModule(namespace, function, PAYLOAD_V2, false, null, null);
    }

    public static HttpApiModule authorizedHttpApiWithV1PayloadModule(final Namespace namespace,
                                                                     final CloudformationResource function,
                                                                     final CloudformationResource pool,
                                                                     final CloudformationResource poolClient) {
        return new HttpApiModule(namespace, function, PAYLOAD_V1, true, pool, poolClient);
    }

    public static HttpApiModule unauthorizedHttpApiWithV1PayloadModule(final Namespace namespace,
                                                           final CloudformationResource function) {
        return new HttpApiModule(namespace, function, PAYLOAD_V1, false, null, null);
    }

    @Override
    public void apply(final CloudformationTemplateBuilder builder) {
        final CloudformationResource api = httpApi(namespace.id("HttpApi"));

        final CloudformationResource integration = httpApiIntegration(
                namespace.id("HttpApiDefaultIntegration"),
                payloadVersion,
                api,
                function
        );

        final CloudformationResource jwtAuthorizer;
        if (authorized) {
            jwtAuthorizer = jwtAuthorizer(
                    namespace.id("HttpApiJwtAuthorizer"),
                    api,
                    pool,
                    poolClient
            );
            builder.withResources(jwtAuthorizer);
        } else {
            jwtAuthorizer = null;
        }

        final CloudformationResource defaultRoute = defaultRoute(namespace.id("HttpApiDefaultRoute"), api, integration, jwtAuthorizer);
        final CloudformationResource deployment = deployment(namespace.id("HttpApiDeployment"), api, integration, defaultRoute);
        final CloudformationResource stage = stage(namespace.id("HttpApiStage"), api, deployment);

        builder.withResources(api, integration, defaultRoute, deployment, stage);
        builder.withOutputs(cloudformationOutput(namespace.id("HttpApi"), api.reference()));
    }
}
