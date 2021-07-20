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

package de.quantummaid.httpmaid.usecases;

import de.quantummaid.httpmaid.CoreModule;
import de.quantummaid.httpmaid.chains.*;
import de.quantummaid.httpmaid.closing.ClosingActions;
import de.quantummaid.httpmaid.generator.GenerationCondition;
import de.quantummaid.httpmaid.generator.Generator;
import de.quantummaid.httpmaid.handler.distribution.DistributableHandler;
import de.quantummaid.httpmaid.handler.distribution.HandlerDistributors;
import de.quantummaid.httpmaid.marshalling.MarshallingModule;
import de.quantummaid.httpmaid.startupchecks.StartupChecks;
import de.quantummaid.httpmaid.usecases.eventfactories.EventFactory;
import de.quantummaid.httpmaid.usecases.eventfactories.enriching.Event;
import de.quantummaid.httpmaid.usecases.eventfactories.enriching.PerEventEnrichers;
import de.quantummaid.httpmaid.usecases.eventfactories.enriching.enrichers.PathParameterEnricher;
import de.quantummaid.httpmaid.usecases.eventfactories.extraction.PerEventExtractors;
import de.quantummaid.httpmaid.usecases.eventfactories.extraction.ResponseMapExtractor;
import de.quantummaid.httpmaid.usecases.mapmaid.ReturnValueSerializer;
import de.quantummaid.httpmaid.websockets.broadcast.Broadcasters;
import de.quantummaid.injectmaid.api.InjectorConfiguration;
import de.quantummaid.mapmaid.MapMaid;
import de.quantummaid.mapmaid.builder.recipes.Recipe;
import de.quantummaid.mapmaid.mapper.deserialization.validation.AggregatedValidationException;
import de.quantummaid.reflectmaid.GenericType;
import de.quantummaid.reflectmaid.ReflectMaid;
import de.quantummaid.reflectmaid.resolvedtype.ResolvedType;
import de.quantummaid.usecasemaid.RoutingTarget;
import de.quantummaid.usecasemaid.UseCaseMaid;
import de.quantummaid.usecasemaid.UseCaseMaidBuilder;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.*;
import java.util.function.Consumer;

import static de.quantummaid.httpmaid.CoreModule.REFLECT_MAID;
import static de.quantummaid.httpmaid.HttpMaidChainKeys.RESPONSE_BODY_OBJECT;
import static de.quantummaid.httpmaid.HttpMaidChains.*;
import static de.quantummaid.httpmaid.chains.ChainName.chainName;
import static de.quantummaid.httpmaid.chains.MetaDataKey.metaDataKey;
import static de.quantummaid.httpmaid.chains.rules.Drop.drop;
import static de.quantummaid.httpmaid.chains.rules.Jump.jumpTo;
import static de.quantummaid.httpmaid.closing.ClosingActions.CLOSING_ACTIONS;
import static de.quantummaid.httpmaid.generator.Generator.generator;
import static de.quantummaid.httpmaid.generator.Generators.generators;
import static de.quantummaid.httpmaid.handler.distribution.DistributableHandler.distributableHandler;
import static de.quantummaid.httpmaid.handler.distribution.HandlerDistributors.HANDLER_DISTRIBUTORS;
import static de.quantummaid.httpmaid.marshalling.MarshallingModule.emptyMarshallingModule;
import static de.quantummaid.httpmaid.serialization.Serializer.SERIALIZER;
import static de.quantummaid.httpmaid.startupchecks.StartupChecks.STARTUP_CHECKS;
import static de.quantummaid.httpmaid.usecases.DetermineRoutingTargetProcessor.determineRoutingTargetProcessor;
import static de.quantummaid.httpmaid.usecases.RegisterSerializerProcessor.registerSerializerProcessor;
import static de.quantummaid.httpmaid.usecases.eventfactories.GenericEventFactory.genericEventFactory;
import static de.quantummaid.httpmaid.usecases.eventfactories.enriching.PerEventEnrichers.perEventEnrichers;
import static de.quantummaid.httpmaid.usecases.eventfactories.enriching.enrichers.PathParameterEnricher.pathParameterEnricher;
import static de.quantummaid.httpmaid.usecases.eventfactories.extraction.PerEventExtractors.perEventExtractors;
import static de.quantummaid.httpmaid.usecases.mapmaid.MapMaidMarshallingMapper.mapMaidMarshallingMapper;
import static de.quantummaid.httpmaid.usecases.mapmaid.MapMaidValidationExceptionMapper.mapMaidValidationExceptionMapper;
import static de.quantummaid.httpmaid.usecases.mapmaid.ReturnValueSerializer.returnValueSerializer;
import static de.quantummaid.httpmaid.usecases.processors.BroadcastingProcessor.broadcastingProcessor;
import static de.quantummaid.httpmaid.usecases.processors.ConstructEventMapProcessor.constructEventMapProcessor;
import static de.quantummaid.httpmaid.usecases.processors.DispatchEventProcessor.dispatchEventProcessor;
import static de.quantummaid.httpmaid.usecases.processors.PerRequestEnrichersProcessor.enrichersProcessor;
import static de.quantummaid.httpmaid.usecases.processors.PerRequestExtractorsProcessor.extractorsProcessor;
import static de.quantummaid.httpmaid.usecases.processors.UnwrapDispatchingExceptionProcessor.unwrapDispatchingExceptionProcessor;
import static de.quantummaid.httpmaid.util.Validators.validateNotNull;
import static de.quantummaid.httpmaid.websockets.broadcast.Broadcasters.BROADCASTERS;
import static de.quantummaid.reflectmaid.GenericType.fromResolvedType;
import static de.quantummaid.reflectmaid.GenericType.genericType;
import static de.quantummaid.usecasemaid.RoutingTarget.routingTarget;
import static de.quantummaid.usecasemaid.UseCaseMaid.aUseCaseMaid;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@SuppressWarnings("java:S1905")
public final class UseCasesModule implements ChainModule {
    public static final MetaDataKey<RoutingTarget> ROUTING_TARGET = metaDataKey("ROUTING_TARGET");
    public static final MetaDataKey<Event> EVENT = metaDataKey("EVENT");
    public static final MetaDataKey<Optional<Object>> RECEIVED_EVENT = metaDataKey("RECEIVED_EVENT");

    public static final ChainName MAP_REQUEST_TO_EVENT = chainName("MAP_REQUEST_TO_EVENT");
    public static final ChainName SUBMIT_EVENT = chainName("SUBMIT_EVENT");
    public static final ChainName MAP_EVENT_TO_RESPONSE = chainName("MAP_EVENT_TO_RESPONSE");
    public static final ChainName EXTERNAL_EVENT = chainName("EXTERNAL_EVENT");

    private static final int DEFAULT_VALIDATION_ERROR_STATUS_CODE = 400;

    private final Map<GenerationCondition, RoutingTarget> useCaseClasses = new LinkedHashMap<>();
    private final List<Generator<RoutingTarget>> routingTargetGenerators = new LinkedList<>();
    private final List<Recipe> mapperConfigurations = new LinkedList<>();
    private final List<InjectorConfiguration> globalScopedInjectorConfigurations = new LinkedList<>();
    private final List<InjectorConfiguration> requestScopedInjectorConfigurations = new LinkedList<>();

    private final List<Generator<RoutingTarget>> eventTypeGenerators = new LinkedList<>();
    private final Map<RoutingTarget, EventFactory> eventFactories = new HashMap<>();
    private final Map<RoutingTarget, PerEventEnrichers> enrichers = new HashMap<>();
    private final Map<RoutingTarget, PerEventExtractors> extractors = new HashMap<>();
    private final List<ResponseMapExtractor> responseMapExtractors = new LinkedList<>();
    private UseCaseMaid useCaseMaid;

    private boolean addAggregatedExceptionHandler = true;
    private int validationErrorStatusCode = DEFAULT_VALIDATION_ERROR_STATUS_CODE;

    public static UseCasesModule useCasesModule() {
        return new UseCasesModule();
    }

    public void addMapperConfiguration(final Recipe recipe) {
        this.mapperConfigurations.add(recipe);
    }

    public void addGlobalScopedInjectorConfiguration(final InjectorConfiguration injectorConfiguration) {
        this.globalScopedInjectorConfigurations.add(injectorConfiguration);
    }

    public void addRequestScopedInjectorConfiguration(final InjectorConfiguration injectorConfiguration) {
        this.requestScopedInjectorConfigurations.add(injectorConfiguration);
    }

    public void addResponseMapExtractor(final ResponseMapExtractor extractor) {
        validateNotNull(extractor, "extractor");
        responseMapExtractors.add(extractor);
    }

    public void addEnricher(final RoutingTarget eventType, final Consumer<PerEventEnrichers> enricher) {
        enrichers.computeIfAbsent(eventType, x -> perEventEnrichers());
        final PerEventEnrichers perEventEnrichers = enrichers.get(eventType);
        enricher.accept(perEventEnrichers);
    }

    public void addExtractor(final RoutingTarget routingTarget, final Consumer<PerEventExtractors> extractor) {
        extractors.computeIfAbsent(routingTarget, x -> perEventExtractors());
        final PerEventExtractors perEventExtractors = extractors.get(routingTarget);
        extractor.accept(perEventExtractors);
    }

    public void doNotAddAggregatedExceptionHandler() {
        this.addAggregatedExceptionHandler = false;
    }

    public void setValidationErrorStatusCode(final int validationErrorStatusCode) {
        this.validationErrorStatusCode = validationErrorStatusCode;
    }

    @Override
    public void init(final MetaData configurationMetaData) {
        final ReflectMaid reflectMaid = configurationMetaData.get(REFLECT_MAID);

        final HandlerDistributors handlerDistributors = configurationMetaData.get(HANDLER_DISTRIBUTORS);
        handlerDistributors.register(handler -> handler.handler() instanceof GenericType, handler -> {
            final GenericType<?> useCaseClass = (GenericType<?>) handler.handler();
            final ResolvedType resolvedUseCaseClass = reflectMaid.resolve(useCaseClass);
            final DistributableHandler followUpHandler = distributableHandler(handler.condition(), routingTarget(resolvedUseCaseClass), handler.perRouteConfigurators());
            return singletonList(followUpHandler);
        });
        handlerDistributors.register(handler -> handler.handler() instanceof Class, handler -> {
            final Class<?> clazz = (Class<?>) handler.handler();
            final GenericType<?> useCaseClass = genericType(clazz);
            final ResolvedType resolvedUseCaseClass = reflectMaid.resolve(useCaseClass);
            final DistributableHandler followUpHandler = distributableHandler(handler.condition(), routingTarget(resolvedUseCaseClass), handler.perRouteConfigurators());
            return singletonList(followUpHandler);
        });
        handlerDistributors.register(handler -> handler.handler() instanceof RoutingTarget, handler -> {
            final RoutingTarget routingTarget = (RoutingTarget) handler.handler();
            useCaseClasses.put(handler.condition(), routingTarget);
            return emptyList();
        });
    }

    @Override
    public List<ChainModule> supplyModulesIfNotAlreadyPresent() {
        return List.of(emptyMarshallingModule());
    }

    @Override
    public void configure(final DependencyRegistry dependencyRegistry) {
        final ReflectMaid reflectMaid = dependencyRegistry.getMetaDatum(REFLECT_MAID);
        final UseCaseMaidBuilder useCaseMaidBuilder = aUseCaseMaid(reflectMaid);
        useCaseClasses.forEach((condition, routingTarget) -> {
            useCaseMaidBuilder.invoking(routingTarget);
            final Generator<RoutingTarget> eventTypeGenerator = generator(routingTarget, condition);
            routingTargetGenerators.add(eventTypeGenerator);
            final List<String> pathParameters = condition.pathParameters();
            pathParameters.forEach(name -> {
                final PathParameterEnricher enricher = pathParameterEnricher(name, name);
                addEnricher(routingTarget, perEventEnrichers -> perEventEnrichers.addPathParameterEnricher(enricher));
            });
        });
        mapperConfigurations.forEach(useCaseMaidBuilder::withMapperConfiguration);
        requestScopedInjectorConfigurations.forEach(useCaseMaidBuilder::withInvocationScopedDependencies);
        globalScopedInjectorConfigurations.forEach(useCaseMaidBuilder::withDependencies);
        final Broadcasters broadcasters = dependencyRegistry.getMetaDatum(BROADCASTERS);
        broadcasters.injectionTypes().forEach(injectionType -> useCaseMaidBuilder
                .withMapperConfiguration(builder -> builder.injecting(injectionType)));
        final List<ResolvedType> messageTypes = broadcasters.messageTypes();
        messageTypes.forEach(resolvedType -> useCaseMaidBuilder
                .withMapperConfiguration(builder -> builder.serializing(fromResolvedType(resolvedType))));
        useCaseMaid = useCaseMaidBuilder.build();
        useCaseClasses.values().forEach(routingTarget -> {
            final Collection<String> parameterNames = useCaseMaid.topLevelParameterNamesFor(routingTarget);
            final EventFactory eventFactory = genericEventFactory(parameterNames);
            eventFactories.put(routingTarget, eventFactory);
        });
        final MapMaid mapMaid = useCaseMaid.mapper();
        final MarshallingModule marshallingModule = dependencyRegistry.getDependency(MarshallingModule.class);
        mapMaidMarshallingMapper().mapMarshalling(mapMaid, marshallingModule);
        final StartupChecks startupChecks = dependencyRegistry.getMetaDatum(STARTUP_CHECKS);
        startupChecks.addStartupCheck(useCaseMaid::runStartupChecks);
        if (addAggregatedExceptionHandler) {
            final CoreModule coreModule = dependencyRegistry.getDependency(CoreModule.class);
            coreModule.addExceptionMapper(AggregatedValidationException.class::isInstance,
                    mapMaidValidationExceptionMapper(validationErrorStatusCode));
        }
    }

    @Override
    public void register(final ChainExtender extender) {
        final ClosingActions closingActions = extender.getMetaDatum(CLOSING_ACTIONS);
        closingActions.addClosingAction(() -> useCaseMaid.instantiator().close());

        final MapMaid mapMaid = useCaseMaid.mapper();
        final ReturnValueSerializer returnValueSerializer = returnValueSerializer(mapMaid);
        extender.appendProcessor(INIT, registerSerializerProcessor(returnValueSerializer));
        extender.addMetaDatum(SERIALIZER, returnValueSerializer);

        extender.appendProcessor(DETERMINE_HANDLER, determineRoutingTargetProcessor(generators(routingTargetGenerators)));
        extender.routeIfSet(PREPARE_RESPONSE, jumpTo(MAP_REQUEST_TO_EVENT), ROUTING_TARGET);

        extender.createChain(MAP_REQUEST_TO_EVENT, jumpTo(SUBMIT_EVENT), jumpTo(EXCEPTION_OCCURRED));
        extender.appendProcessor(MAP_REQUEST_TO_EVENT, constructEventMapProcessor(eventFactories));
        extender.appendProcessor(MAP_REQUEST_TO_EVENT, enrichersProcessor(enrichers));

        final Broadcasters broadcasters = extender.getMetaDatum(BROADCASTERS);
        extender.appendProcessor(MAP_REQUEST_TO_EVENT, broadcastingProcessor(broadcasters));

        extender.createChain(SUBMIT_EVENT, jumpTo(MAP_EVENT_TO_RESPONSE), jumpTo(EXCEPTION_OCCURRED));
        extender.appendProcessor(SUBMIT_EVENT, dispatchEventProcessor(useCaseMaid));

        extender.createChain(MAP_EVENT_TO_RESPONSE, jumpTo(POST_INVOKE), jumpTo(EXCEPTION_OCCURRED));
        responseMapExtractors.forEach(extractor -> extender.appendProcessor(MAP_EVENT_TO_RESPONSE, extractor));
        extender.appendProcessor(MAP_EVENT_TO_RESPONSE, extractorsProcessor(extractors));

        extender.appendProcessor(MAP_EVENT_TO_RESPONSE, metaData -> {
            final Object map = metaData.get(RECEIVED_EVENT).orElseGet(HashMap::new);
            metaData.set(RESPONSE_BODY_OBJECT, map);
        });

        extender.appendProcessor(PREPARE_EXCEPTION_RESPONSE, unwrapDispatchingExceptionProcessor());
        extender.createChain(EXTERNAL_EVENT, drop(), jumpTo(EXCEPTION_OCCURRED));
    }
}
