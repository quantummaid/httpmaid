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

package websockets.givenwhenthen.configurations.artificial;

import com.google.gson.Gson;
import de.quantummaid.httpmaid.HttpMaid;
import de.quantummaid.httpmaid.path.Path;
import de.quantummaid.httpmaid.security.SecurityConfigurators;
import de.quantummaid.httpmaid.websockets.registry.WebSocketRegistry;
import de.quantummaid.eventmaid.messageBus.MessageBus;
import de.quantummaid.eventmaid.messageBus.MessageBusType;
import de.quantummaid.eventmaid.useCases.building.ExceptionSerializationStep1Builder;
import de.quantummaid.eventmaid.useCases.building.Step1Builder;
import de.quantummaid.eventmaid.useCases.useCaseAdapter.UseCaseAdapter;
import websockets.givenwhenthen.configurations.TestConfiguration;
import websockets.givenwhenthen.configurations.artificial.usecases.abc.UseCaseA;
import websockets.givenwhenthen.configurations.artificial.usecases.abc.UseCaseB;
import websockets.givenwhenthen.configurations.artificial.usecases.abc.UseCaseC;
import websockets.givenwhenthen.configurations.artificial.usecases.both.BothUseCase;
import websockets.givenwhenthen.configurations.artificial.usecases.close.CloseUseCase;
import websockets.givenwhenthen.configurations.artificial.usecases.count.CountUseCase;
import websockets.givenwhenthen.configurations.artificial.usecases.echo.EchoParameter;
import websockets.givenwhenthen.configurations.artificial.usecases.echo.EchoUseCase;
import websockets.givenwhenthen.configurations.artificial.usecases.exception.ExceptionUseCase;
import websockets.givenwhenthen.configurations.artificial.usecases.exception.ExceptionUseCaseParameter;
import websockets.givenwhenthen.configurations.artificial.usecases.headers.HeaderParameter;
import websockets.givenwhenthen.configurations.artificial.usecases.headers.HeaderUseCase;
import websockets.givenwhenthen.configurations.artificial.usecases.pathparameter.ParameterParameter;
import websockets.givenwhenthen.configurations.artificial.usecases.pathparameter.ParameterUseCase;
import websockets.givenwhenthen.configurations.artificial.usecases.query.QueryParameter;
import websockets.givenwhenthen.configurations.artificial.usecases.query.QueryUseCase;
import websockets.givenwhenthen.configurations.artificial.usecases.queryfoo.QueryFooUseCase;

import java.util.Map;

import static de.quantummaid.httpmaid.HttpMaid.anHttpMaid;
import static de.quantummaid.httpmaid.HttpMaidChainKeys.REQUEST_BODY_MAP;
import static de.quantummaid.httpmaid.chains.Configurator.toUseModules;
import static de.quantummaid.httpmaid.events.EventConfigurators.toEnrichTheIntermediateMapWithAllRequestData;
import static de.quantummaid.httpmaid.events.EventConfigurators.toUseTheMessageBus;
import static de.quantummaid.httpmaid.events.EventModule.eventModule;
import static de.quantummaid.httpmaid.http.headers.ContentType.json;
import static de.quantummaid.httpmaid.logger.LoggerConfigurators.toLogUsing;
import static de.quantummaid.httpmaid.logger.Loggers.stderrLogger;
import static de.quantummaid.httpmaid.marshalling.MarshallingConfigurators.*;
import static de.quantummaid.httpmaid.security.SecurityConfigurators.toAuthenticateRequestsUsing;
import static de.quantummaid.httpmaid.websockets.WebSocketsConfigurator.toUseWebSockets;
import static de.quantummaid.httpmaid.websockets.WebsocketChainKeys.WEBSOCKET_REGISTRY;
import static de.quantummaid.httpmaid.websocketsevents.Conditions.webSocketIsTaggedWith;
import static de.quantummaid.eventmaid.configuration.AsynchronousConfiguration.constantPoolSizeAsynchronousConfiguration;
import static de.quantummaid.eventmaid.messageBus.MessageBusBuilder.aMessageBus;
import static de.quantummaid.eventmaid.processingContext.EventType.eventTypeFromString;
import static de.quantummaid.eventmaid.useCases.useCaseAdapter.UseCaseInvocationBuilder.anUseCaseAdapter;
import static websockets.givenwhenthen.configurations.TestConfiguration.testConfiguration;
import static websockets.givenwhenthen.configurations.artificial.usecases.echo.EchoParameter.echoParameter;
import static websockets.givenwhenthen.configurations.artificial.usecases.exception.ExceptionUseCaseParameter.exceptionUseCaseParameter;
import static websockets.givenwhenthen.configurations.artificial.usecases.headers.HeaderParameter.headerParameter;
import static websockets.givenwhenthen.configurations.artificial.usecases.pathparameter.ParameterParameter.parameterParameter;
import static websockets.givenwhenthen.configurations.artificial.usecases.query.QueryParameter.queryParameter;

public final class ArtificialConfiguration {
    private static final int POOL_SIZE = 4;
    public static volatile MessageBus messageBus;

    private ArtificialConfiguration() {
    }

    @SuppressWarnings("unchecked")
    public static TestConfiguration theExampleHttpMaidInstanceWithWebSocketsSupport() {
        messageBus = aMessageBus()
                .forType(MessageBusType.ASYNCHRONOUS)
                .withAsynchronousConfiguration(constantPoolSizeAsynchronousConfiguration(POOL_SIZE))
                .build();

        final Step1Builder step1Builder = anUseCaseAdapter();
        addUseCase(BothUseCase.class, "BothUseCase", step1Builder);
        addUseCase(CountUseCase.class, "CountUseCase", step1Builder);
        addUseCase(CloseUseCase.class, "CloseUseCase", step1Builder);
        addUseCase(QueryFooUseCase.class, "QueryFooUseCase", step1Builder);
        addUseCase(UseCaseA.class, "UseCaseA", step1Builder);
        addUseCase(UseCaseB.class, "UseCaseB", step1Builder);
        addUseCase(UseCaseC.class, "UseCaseC", step1Builder);
        addUseCase(ExceptionUseCase.class, "ExceptionUseCaseParameter", step1Builder);
        addUseCase(QueryUseCase.class, "QueryParameter", step1Builder);
        addUseCase(HeaderUseCase.class, "HeaderParameter", step1Builder);
        addUseCase(ParameterUseCase.class, "ParameterParameter", step1Builder);
        final ExceptionSerializationStep1Builder exceptionSerializationStep1Builder = addUseCase(EchoUseCase.class, "EchoParameter", step1Builder);
        final UseCaseAdapter useCaseAdapter = exceptionSerializationStep1Builder
                .puttingExceptionObjectNamedAsExceptionIntoResponseMapByDefault()
                .throwAnExceptionByDefaultIfNoResponseDeserializationCanBeApplied()
                .buildAsStandaloneAdapter();

        useCaseAdapter.attachAndEnhance(messageBus);

        final HttpMaid httpMaid = anHttpMaid()
                .disableAutodectectionOfModules()
                .get("/normal", eventTypeFromString("NormalUseCase"))
                .get("/both", eventTypeFromString("BothUseCase"))
                .serving(eventTypeFromString("CloseUseCase")).when(webSocketIsTaggedWith("CLOSE"))
                .serving(eventTypeFromString("CountUseCase")).when(webSocketIsTaggedWith("COUNT"))
                .serving(eventTypeFromString("UseCaseA")).when(metaData -> metaData.get(REQUEST_BODY_MAP).getOrDefault("useCase", "").equals("A"))
                .serving(eventTypeFromString("UseCaseB")).when(metaData -> metaData.get(REQUEST_BODY_MAP).getOrDefault("useCase", "").equals("B"))
                .serving(eventTypeFromString("UseCaseC")).when(metaData -> metaData.get(REQUEST_BODY_MAP).getOrDefault("useCase", "").equals("C"))
                .serving(eventTypeFromString("QueryFooUseCase")).when(webSocketIsTaggedWith("QUERY_FOO"))
                .serving(eventTypeFromString("ExceptionUseCaseParameter")).when(webSocketIsTaggedWith("EXCEPTION"))
                .serving(eventTypeFromString("EchoParameter")).when(webSocketIsTaggedWith("ECHO"))
                .serving(eventTypeFromString("ParameterParameter")).when(webSocketIsTaggedWith("PARAMETERIZED"))
                .serving(eventTypeFromString("QueryParameter")).when(webSocketIsTaggedWith("QUERY"))
                .serving(eventTypeFromString("HeaderParameter")).when(webSocketIsTaggedWith("HEADER"))

                .configured(toUseTheMessageBus(messageBus))

                .configured(toUnmarshallContentTypeInRequests(json(), string -> new Gson().fromJson(string, Map.class)))
                .configured(toMarshallContentTypeInResponses(json(), map -> new Gson().toJson(map)))
                .configured(toMarshallByDefaultUsingTheContentType(json()))

                .configured(toAuthenticateRequestsUsing(request -> request.queryParameters().getOptionalQueryParameter("username")).notFailingOnMissingAuthentication())
                .configured(toAuthenticateRequestsUsing(request -> request.headers().getOptionalHeader("username")).notFailingOnMissingAuthentication())
                .configured(SecurityConfigurators.toAuthorizeRequestsUsing((authenticationInformation, request) -> {
                    final Path path = request.path();
                    if (path.matches("/authorized")) {
                        return authenticationInformation
                                .map("admin"::equals)
                                .orElse(false);
                    }
                    return true;
                }))
                .configured(toLogUsing(stderrLogger()))
                .configured(toUseWebSockets()
                        .acceptingWebSocketsToThePath("/").taggedBy("ROOT")
                        .acceptingWebSocketsToThePath("/close").taggedBy("CLOSE")
                        .acceptingWebSocketsToThePath("/both").taggedBy("BOTH")
                        .acceptingWebSocketsToThePath("/authorized").taggedBy("AUTHORIZED")
                        .acceptingWebSocketsToThePath("/count").taggedBy("COUNT")
                        .acceptingWebSocketsToThePath("/query_foo").taggedBy("QUERY_FOO")
                        .acceptingWebSocketsToThePath("/echo").taggedBy("ECHO")
                        .acceptingWebSocketsToThePath("/pre/<var>/post").taggedBy("PARAMETERIZED")
                        .acceptingWebSocketsToThePath("/query").taggedBy("QUERY")
                        .acceptingWebSocketsToThePath("/header").taggedBy("HEADER")
                        .acceptingWebSocketsToThePath("/exception").taggedBy("EXCEPTION"))

                .configured(toEnrichTheIntermediateMapWithAllRequestData())
                .configured(toUseModules(eventModule()))
                .build();

        final WebSocketRegistry webSocketRegistry = httpMaid.getMetaDatum(WEBSOCKET_REGISTRY);
        messageBus.subscribe(eventTypeFromString("CloseEvent"),
                o -> webSocketRegistry.allActiveWebSockets().forEach(webSocket -> {
                    webSocketRegistry.unregister(webSocket.id());
                    webSocket.close();
                }));

        return testConfiguration(httpMaid);
    }

    @SuppressWarnings("unchecked")
    private static ExceptionSerializationStep1Builder addUseCase(final Class<?> useCase,
                                                                 final String event,
                                                                 final Step1Builder step1Builder) {
        final ExceptionSerializationStep1Builder exceptionSerializationStep1Builder = step1Builder.invokingUseCase(useCase).forType(event).callingTheSingleUseCaseMethod()
                .obtainingUseCaseInstancesUsingTheZeroArgumentConstructor()
                .throwingAnExceptionByDefaultIfNoRequestSerializationCanBeApplied()

                .deserializingRequestsToUseCaseParametersOfType(QueryParameter.class).using((targetType, map) -> queryParameter((String) ((Map<String, Object>) map).get("var")))
                .deserializingRequestsToUseCaseParametersOfType(HeaderParameter.class).using((targetType, map) -> headerParameter((String) ((Map<String, Object>) map).get("var")))
                .deserializingRequestsToUseCaseParametersOfType(ParameterParameter.class).using((targetType, map) -> parameterParameter((String) ((Map<String, Object>) map).get("var")))
                .deserializingRequestsToUseCaseParametersOfType(EchoParameter.class).using((targetType, map) -> echoParameter((String) ((Map<String, Object>) map).get("echoValue")))
                .deserializingRequestsToUseCaseParametersOfType(ExceptionUseCaseParameter.class).using((targetType, map) -> exceptionUseCaseParameter((String) ((Map<String, Object>) map).get("mode")))
                .throwAnExceptionByDefaultIfNoUseCaseRequestDeserializationCanBeApplied()

                .serializingUseCaseResponseBackOntoTheBusOfType(String.class).using(object -> Map.of("stringValue", object))
                .serializingResponseObjectsOfTypeVoid().using(object -> Map.of())
                .throwingAnExceptionByDefaultIfNoResponseSerializationCanBeApplied();
        return exceptionSerializationStep1Builder;
    }
}
