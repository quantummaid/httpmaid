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

package de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation.synthesizer.resources;

import de.quantummaid.httpmaid.remotespecs.lambda.aws.Artifact;
import de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation.synthesizer.CloudformationName;
import de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation.synthesizer.CloudformationResource;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation.synthesizer.CloudformationResource.cloudformationResource;
import static de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation.synthesizer.IntrinsicFunctions.sub;
import static de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation.synthesizer.PseudoParameters.STACK_NAME;
import static java.lang.String.format;

public final class Lambda {

    private Lambda() {
    }

    public static CloudformationResource function(final CloudformationName resourceId,
                                                  final Artifact artifact,
                                                  final CloudformationResource functionRole,
                                                  final LambdaPayload payload,
                                                  final Map<String, Object> environment
    ) {
        final Map<String, Object> fullEnvironment = new LinkedHashMap<>(payload.additionalEnvironmentVariables);
        fullEnvironment.putAll(environment);
        return cloudformationResource(resourceId, "AWS::Lambda::Function", Map.of(
                "FunctionName", resourceId.asId(),
                "Code", Map.of(
                        "S3Bucket", artifact.bucket(),
                        "S3Key", artifact.object()
                ),
                "Tags", List.of(
                        Map.of(
                                "Key", "StackIdentifier",
                                "Value", sub(STACK_NAME)
                        )
                ),
                "MemorySize", 512,
                "Role", functionRole.attribute("Arn"),
                "Timeout", 20,
                "Runtime", payload.runtime,
                "Handler", payload.handler,
                "Environment", Map.of("Variables", fullEnvironment)
        ));
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class LambdaPayload {
        private final String runtime;
        private final String handler;
        private final Map<String, String> additionalEnvironmentVariables;

        public static LambdaPayload java11Payload(final String handlerClass,
                                                  final String handlerMethod) {
            final String handler = format("%s::%s", handlerClass, handlerMethod);
            return new LambdaPayload("java11", handler, Map.of());
        }

        public static LambdaPayload providedPayload(final String entryPoint) {
            return new LambdaPayload("provided", "thisisignored", Map.of("CUSTOM_ENTRYPOINT", entryPoint));
        }
    }
}
