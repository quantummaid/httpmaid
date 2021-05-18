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

package de.quantummaid.httpmaid.remotespecs.lambda.aws;

import de.quantummaid.httpmaid.remotespecs.RemoteSpecs;
import de.quantummaid.httpmaid.remotespecs.RemoteSpecsDeployer;
import de.quantummaid.httpmaid.remotespecs.RemoteSpecsDeployment;
import de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation.CloudFormationHandler;
import de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation.synthesizer.*;
import de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation.template.StringTemplate;
import de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation.template.Template;
import de.quantummaid.httpmaid.remotespecs.lambda.junit.Listener;
import de.quantummaid.httpmaid.tests.givenwhenthen.basedirectory.BaseDirectoryFinder;
import de.quantummaid.httpmaid.tests.givenwhenthen.deploy.Deployment;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

import static de.quantummaid.httpmaid.remotespecs.lambda.aws.Artifacts.artifacts;
import static de.quantummaid.httpmaid.remotespecs.lambda.aws.StackIdentifier.sharedStackIdentifier;
import static de.quantummaid.httpmaid.remotespecs.lambda.aws.StackIdentifier.userProvidedStackIdentifier;
import static de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation.CloudFormationHandler.connectToCloudFormation;
import static de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation.synthesizer.CloudformationName.cloudformationName;
import static de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation.synthesizer.CloudformationOutput.cloudformationOutput;
import static de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation.synthesizer.CloudformationTemplateBuilder.cloudformationTemplateBuilder;
import static de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation.synthesizer.IntrinsicFunctions.sub;
import static de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation.synthesizer.PseudoParameters.REGION;
import static de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation.synthesizer.StackOutputs.stackOutputs;
import static de.quantummaid.httpmaid.remotespecs.lambda.aws.cloudformation.template.S3Template.s3Template;
import static de.quantummaid.httpmaid.remotespecs.lambda.aws.cognito.Cognito.generateValidAccessToken;
import static de.quantummaid.httpmaid.remotespecs.lambda.aws.s3.S3Handler.deleteAllObjectsInBucket;
import static de.quantummaid.httpmaid.remotespecs.lambda.aws.s3.S3Handler.uploadToS3Bucket;
import static java.util.Optional.empty;

/**
 * When stackIdentifier is not user-supplied (developerMode == false), then
 * - the default stackIdentifier is: httpmaid-remotespecs-${AWS::AccountId}
 * - the default stack naming scheme is: ${stackIdentifier}-(lambda|bucket)
 * - The bucket is owned by ??? (whoever runs through the test setup first)
 * - Any other user needing access must have that access granted some other way
 * (additional policy, etc...). Users who cannot do that simply define
 * a user-supplied stackidentifier.
 * - cleanup policy
 * - bucket stack remains
 * - lambda stack is deleted
 * <p>
 * When stackIdentifier is user-supplied (developerMode == true), we're in
 * the "per-user-infra" mode.
 * - $stackIdentifier-lambda is created, owned by the user running the test
 * - $stackIdentifier-bucket is created, ditto
 * - cleanup policy
 * - bucket stack remains
 * - lambda stack remains
 * (ie Both must be cleaned up / managed by the user)
 */
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class LambdaDeployer implements RemoteSpecsDeployer {
    private static final String RELATIVE_PATH_TO_LAMBDA_JAR = "/tests/lambda/target/remotespecs.jar";
    private static final String RELATIVE_PATH_TO_LAMBDA_ZIP = "/tests/lambda/target/remotespecs.zip";

    private final StackIdentifier stackIdentifier;
    private final Boolean developerMode;

    public static LambdaDeployer lambdaDeployer() {
        return userProvidedStackIdentifier()
                .map(LambdaDeployer::perUserLambdaDeployer)
                .orElseGet(LambdaDeployer::sharedLambdaDeployer);
    }

    private static LambdaDeployer perUserLambdaDeployer(final StackIdentifier stackIdentifier) {
        return new LambdaDeployer(stackIdentifier, true);
    }

    private static LambdaDeployer sharedLambdaDeployer() {
        return new LambdaDeployer(sharedStackIdentifier(), false);
    }

    @Override
    public RemoteSpecsDeployment deploy() {
        final String artifactBucketName = stackIdentifier.value() + "-bucket";

        final CloudformationTemplate bucketTemplate = Templates.bucketTemplate(artifactBucketName);
        create(artifactBucketName, StringTemplate.stringTemplate(bucketTemplate));
        final Artifact jarArtifact = upload(RELATIVE_PATH_TO_LAMBDA_JAR, artifactBucketName);
        final Artifact zipArtifact = upload(RELATIVE_PATH_TO_LAMBDA_ZIP, artifactBucketName);
        final Artifacts artifacts = artifacts(jarArtifact, zipArtifact);

        final List<Class<?>> relevantClassSources = Listener.getClassSources().stream()
                .filter(this::hasCloudformationRequirements)
                .collect(Collectors.toList());

        final Namespace namespace = Namespace.namespace(stackIdentifier.value());
        final CloudformationTemplate lambdaTemplate = buildTemplate(relevantClassSources, namespace, artifacts);
        final String renderedTemplate = lambdaTemplate.render();
        log.debug(renderedTemplate);
        final String templateArtifactName = uploadToS3Bucket(artifactBucketName, renderedTemplate);
        final Map<String, String> stackOutputs = create(stackIdentifier.value() + "-lambda", s3Template(artifactBucketName, templateArtifactName));

        final Map<Class<? extends RemoteSpecs>, Deployment> deploymentMap = buildDeploymentMap(relevantClassSources, stackOutputs, namespace);

        return RemoteSpecsDeployment.remoteSpecsDeployment(this::cleanUp, deploymentMap);
    }

    private Artifact upload(final String relativePath, final String artifactBucketName) {
        final String basePath = BaseDirectoryFinder.findProjectBaseDirectory();
        final String lambdaPath = basePath + relativePath;
        final File file = new File(lambdaPath);
        final String s3Key = uploadToS3Bucket(artifactBucketName, file);
        return Artifact.artifact(artifactBucketName, s3Key);
    }

    private CloudformationTemplate buildTemplate(final List<Class<?>> relevantTestClasses,
                                                 final Namespace namespace,
                                                 final Artifacts artifacts) {
        final CloudformationTemplateBuilder builder = cloudformationTemplateBuilder();
        relevantTestClasses.stream()
                .map(aClass -> extractModuleFromTestClass(aClass, namespace, artifacts))
                .flatMap(Optional::stream)
                .forEach(builder::withModule);
        return builder
                .withOutputs(cloudformationOutput(cloudformationName("Region"), sub(REGION)))
                .build();
    }

    private boolean hasCloudformationRequirements(final Class<?> testClass) {
        return infrastructureRequirementsMethod(testClass).isPresent();
    }

    private Optional<CloudformationModule> extractModuleFromTestClass(final Class<?> testClass,
                                                                      final Namespace parentNamespace,
                                                                      final Artifacts artifacts) {
        return infrastructureRequirementsMethod(testClass)
                .map(method -> {
                    final Namespace namespace = parentNamespace.sub(testClass.getSimpleName());
                    return invokeInfrastructureRequirementsMethod(method, namespace, artifacts);
                });
    }

    private Optional<Method> infrastructureRequirementsMethod(final Class<?> testClass) {
        try {
            final Method method = testClass.getMethod(
                    "infrastructureRequirements",
                    Namespace.class,
                    Artifacts.class
            );
            return Optional.of(method);
        } catch (final NoSuchMethodException e) {
            return empty();
        }
    }

    private Method deploymentMethod(final Class<?> testClass) {
        return method(testClass, "loadDeployment", StackOutputs.class, Namespace.class);
    }

    private Method method(final Class<?> testClass,
                          final String name,
                          final Class<?>... parameters) {
        try {
            return testClass.getMethod(name, parameters);
        } catch (final NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }

    private Deployment invokeDeploymentMethod(final Method method,
                                              final Map<String, String> stackOutputs,
                                              final Namespace namespace) {
        final StackOutputs stackOutputsObject = stackOutputs(stackOutputs);
        return invokeStaticMethod(method, stackOutputsObject, namespace);
    }

    private CloudformationModule invokeInfrastructureRequirementsMethod(final Method method,
                                                                        final Namespace namespace,
                                                                        final Artifacts artifacts) {
        return invokeStaticMethod(method, namespace, artifacts);
    }

    @SuppressWarnings("unchecked")
    private <T> T invokeStaticMethod(final Method method,
                                     final Object... parameters) {
        try {
            return (T) method.invoke(null, parameters);
        } catch (final IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<Class<? extends RemoteSpecs>, Deployment> buildDeploymentMap(final List<Class<?>> relevantClassSources,
                                                                             final Map<String, String> stackOutputs,
                                                                             final Namespace parentNamespace) {
        final Map<Class<? extends RemoteSpecs>, Deployment> deploymentMap = new LinkedHashMap<>();
        relevantClassSources.forEach(testClass -> {
            final Method deploymentMethod = deploymentMethod(testClass);
            final Namespace namespace = parentNamespace.sub(testClass.getSimpleName());
            final Deployment deployment = invokeDeploymentMethod(deploymentMethod, stackOutputs, namespace);
            deploymentMap.put((Class<? extends RemoteSpecs>) testClass, deployment);
        });
        return deploymentMap;
    }

    public static String loadToken(final StackOutputs stackOutputs,
                                   final Namespace namespace) {
        final String poolId = stackOutputs.get(namespace.id("PoolId"));
        final String poolClientId = stackOutputs.get(namespace.id("PoolClientId"));
        final String username = UUID.randomUUID().toString();
        return generateValidAccessToken(poolId, poolClientId, username);
    }

    private void cleanUp() {
        if (developerMode) {
            return;
        }
        final String artifactBucketName = stackIdentifier.value() + "-bucket";
        deleteAllObjectsInBucket(artifactBucketName);
        try (CloudFormationHandler cloudFormationHandler = connectToCloudFormation()) {
            cloudFormationHandler.deleteStacksStartingWith(stackIdentifier.value() + "-lambda");
            cloudFormationHandler.deleteStacksStartingWith(stackIdentifier.value() + "-bucket");
        }
    }

    private static Map<String, String> create(
            final String stackName,
            final Template template) {
        try (CloudFormationHandler cloudFormationHandler = connectToCloudFormation()) {
            return cloudFormationHandler.createOrUpdateStack(stackName, template);
        }
    }
}
