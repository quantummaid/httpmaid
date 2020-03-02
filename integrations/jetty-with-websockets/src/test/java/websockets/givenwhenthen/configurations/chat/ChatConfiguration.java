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

package websockets.givenwhenthen.configurations.chat;

import de.quantummaid.eventmaid.messageBus.MessageBus;
import websockets.givenwhenthen.configurations.TestConfiguration;

import static de.quantummaid.eventmaid.configuration.AsynchronousConfiguration.constantPoolSizeAsynchronousConfiguration;
import static de.quantummaid.eventmaid.messageBus.MessageBusBuilder.aMessageBus;
import static de.quantummaid.eventmaid.messageBus.MessageBusType.ASYNCHRONOUS;

public final class ChatConfiguration {
    private static final int POOL_SIZE = 4;
    public static volatile MessageBus messageBus;

    private ChatConfiguration() {
    }

    @SuppressWarnings("unchecked")
    public static TestConfiguration theExampleChatServerHttpMaidInstance() {
        messageBus = aMessageBus()
                .forType(ASYNCHRONOUS)
                .withAsynchronousConfiguration(constantPoolSizeAsynchronousConfiguration(POOL_SIZE))
                .build();

        /*
        final UserRepository userRepository = userRepository();
        final Authenticator<HttpRequest> authenticator = request -> request.headers()
                .getOptionalHeader("user")
                .map(Username::username)
                .map(userRepository::byName);
        final HttpAuthorizer authorizer = (authenticationInformation, request) -> authenticationInformation.isPresent();

        final UseCaseAdapter useCaseAdapter = anUseCaseAdapter()
                .invokingUseCase(SendMessageUseCase.class).forType("ChatMessage")
                .callingTheSingleUseCaseMethod()
                .obtainingUseCaseInstancesUsingTheZeroArgumentConstructor()
                .mappingRequestsToUseCaseParametersOfType(ChatMessage.class).using((type, map) -> {
                    final String content = (String) map.get("content");
                    final String recipient = (String) map.get("recipient");
                    return chatMessage(messageContent(content), username(recipient));
                })
                .throwAnExceptionByDefaultIfNoParameterMappingCanBeApplied()
                .serializingResponseObjectsThat(Objects::isNull).using(object -> null)
                .throwingAnExceptionByDefaultIfNoResponseMappingCanBeApplied()
                .puttingExceptionObjectNamedAsExceptionIntoResponseMapByDefault()
                .buildAsStandaloneAdapter();

        useCaseAdapter.attachAndEnhance(messageBus);

        final HttpMaid httpMaid = anHttpMaid()
                .get("/send", eventTypeFromString("ChatMessage"))
                .configured(toUseModules(eventModule()))
                .configured(toAuthenticateRequestsUsing(authenticator))
                .configured(SecurityConfigurators.toAuthorizeRequestsUsing(authorizer))
                .configured(toLogUsing(stderrLogger()))
                .configured(toUseWebSockets()
                        .acceptingWebSocketsToThePath("/subscribe").saving(AUTHENTICATION_INFORMATION))
                .configured(toUseTheMessageBus(messageBus))

                .configured(configuratorForType(EventModule.class, eventModule ->
                        eventModule.addExternalEventMapping(eventTypeFromString("NewMessageEvent"),
                                forwardingItToAllWebSocketsThat((metaData, event) -> {
                                    final String username = metaData.getAs(AUTHENTICATION_INFORMATION, User.class)
                                            .name().internalValueForMapping();
                                    return Objects.equals(event.get("recipient"), username);
                                }))))

                .configured(toEnrichTheIntermediateMapWithAllRequestData())

                .configured(toMarshallBodiesBy()
                        .unmarshallingContentTypeInRequests(json()).with(string -> new Gson().fromJson(string, Map.class))
                        .marshallingContentTypeInResponses(json()).with(map -> new Gson().toJson(map))
                        .usingTheDefaultContentType(json()))
                .build();

        return testConfiguration(httpMaid);
         */
        throw new UnsupportedOperationException();
    }
}
