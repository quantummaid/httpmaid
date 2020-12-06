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

import de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation.synthesizer.CloudformationResource;
import de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation.synthesizer.JarCoordinates;

import java.util.List;
import java.util.Map;

import static de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation.synthesizer.IntrinsicFunctions.sub;
import static de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation.synthesizer.PseudoParameters.STACK_NAME;
import static java.lang.String.format;

public final class Lambda {

    private Lambda() {
    }

    public static CloudformationResource function(final String resourceId,
                                                  final String functionName,
                                                  final JarCoordinates jarCoordinates,
                                                  final CloudformationResource functionRole,
                                                  final String handlerClass,
                                                  final String handlerMethod,
                                                  final Map<String, Object> environment
    ) {
        final String handler = format("%s::%s", handlerClass, handlerMethod);
        return CloudformationResource.cloudformationResource(resourceId, "AWS::Lambda::Function", Map.of(
                "FunctionName", functionName,
                "Code", Map.of(
                        "S3Bucket", jarCoordinates.bucket(),
                        "S3Key", jarCoordinates.object()
                ),
                "Tags", List.of(
                        Map.of(
                                "Key", "StackIdentifier",
                                "Value", sub(STACK_NAME)
                        )
                ),
                "MemorySize", 512,
                "Handler", handler,
                "Role", functionRole.attribute("Arn"),
                "Timeout", 20,
                "Runtime", "java11",
                "Environment", Map.of("Variables", environment)
        ));
    }
}
