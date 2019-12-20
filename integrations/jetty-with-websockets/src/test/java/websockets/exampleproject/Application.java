/*
 * Copyright (c) 2019 Richard Hauswald - https://quantummaid.de/.
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

package websockets.exampleproject;

import de.quantummaid.messagemaid.messageBus.MessageBus;
import de.quantummaid.messagemaid.messageBus.MessageBusType;
import websockets.exampleproject.domain.User;

import static de.quantummaid.messagemaid.configuration.AsynchronousConfiguration.constantPoolSizeAsynchronousConfiguration;
import static de.quantummaid.messagemaid.messageBus.MessageBusBuilder.aMessageBus;

public final class Application {

    private static final int POOL_SIZE = 4;
    public static final MessageBus MESSAGE_BUS = aMessageBus()
            .forType(MessageBusType.ASYNCHRONOUS)
            .withAsynchronousConfiguration(constantPoolSizeAsynchronousConfiguration(POOL_SIZE))
            .build();

    private Application() {
    }

    public static void startApplication() {

        /*
        final Gson gson = new Gson();

        aDeserializer()
                .withJsonUnmarshaller(gson::fromJson)
                .withCustomPrimitive(MessageContent.class).deserializedUsingTheStaticMethodWithSingleStringArgument()
                .withCustomPrimitive(Username.class).deserializedUsingTheStaticMethodWithSingleStringArgument()
                .withDataTransferObject(SendMessageRequest.class).deserializedUsingTheSingleFactoryMethod()
                .withCustomPrimitive(User.class).deserializedUsing(new DeserializationCPMethod() {
                    @Override
                    public void verifyCompatibility(final Class<?> targetType) {
                    }

                    @Override
                    public Object deserialize(final String input, final Class<?> targetType) {
                        return VERY_STUPID.get();
                    }
                })
                .build();

        aSerializer()
                .withJsonMarshaller(gson::toJson)
                .withDataTransferObject(SendMessageResponse.class).serializedByItsPublicFields()
                .withDataTransferObject(Message.class).serializedByItsPublicFields()
                .withDataTransferObject(NewMessageEvent.class).serializedByItsPublicFields()
                .withCustomPrimitive(MessageContent.class).serializedUsingTheMethod(MessageContent::stringValue)
                .withCustomPrimitive(MessageId.class).serializedUsingTheMethod(MessageId::stringValue)
                .withCustomPrimitive(Username.class).serializedUsingTheMethod(Username::stringValue)
                .build();

        /*
        final UseCaseAdapter useCaseAdapter = anUseCaseAdapter()
                .invokingUseCase(SendMessageUseCase.class).forType("SendMessageRequest").callingTheSingleUseCaseMethod()
                .obtainingUseCaseInstancesUsingTheZeroArgumentConstructor()
                .throwAnExceptionByDefaultIfNoParameterMappingCanBeApplied()
                .throwingAnExceptionByDefaultIfNoResponseMappingCanBeApplied()
                .puttingExceptionObjectNamedAsExceptionIntoResponseMapByDefault()
                .buildAsStandaloneAdapter();
        useCaseAdapter.attachAndEnhance(MESSAGE_BUS);

        AntiHateSpeechUseCase.register();
         */
    }

    private static final ThreadLocal<User> VERY_STUPID = new ThreadLocal<>();

    public static void main(final String[] args) {
        startApplication();
    }
}
