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

package de.quantummaid.httpmaid.events;

import de.quantummaid.eventmaid.messagebus.MessageBus;
import de.quantummaid.eventmaid.processingcontext.EventType;
import de.quantummaid.httpmaid.chains.*;
import de.quantummaid.httpmaid.closing.ClosingActions;
import de.quantummaid.httpmaid.events.enriching.PerEventEnrichers;
import de.quantummaid.httpmaid.events.enriching.enrichers.PathParameterEnricher;
import de.quantummaid.httpmaid.events.extraction.PerEventExtractors;
import de.quantummaid.httpmaid.events.processors.DetermineEventProcessor;
import de.quantummaid.httpmaid.events.processors.DispatchEventProcessor;
import de.quantummaid.httpmaid.generator.GenerationCondition;
import de.quantummaid.httpmaid.generator.Generator;
import de.quantummaid.httpmaid.handler.distribution.HandlerDistributors;
import de.quantummaid.httpmaid.websockets.broadcast.Broadcasters;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.*;
import java.util.function.Consumer;

import static de.quantummaid.eventmaid.configuration.AsynchronousConfiguration.constantPoolSizeAsynchronousConfiguration;
import static de.quantummaid.eventmaid.messagebus.MessageBusBuilder.aMessageBus;
import static de.quantummaid.eventmaid.messagebus.MessageBusType.ASYNCHRONOUS;
import static de.quantummaid.httpmaid.HttpMaidChainKeys.RESPONSE_BODY_OBJECT;
import static de.quantummaid.httpmaid.HttpMaidChains.*;
import static de.quantummaid.httpmaid.chains.ChainRegistry.CHAIN_REGISTRY;
import static de.quantummaid.httpmaid.chains.MetaDataKey.metaDataKey;
import static de.quantummaid.httpmaid.chains.rules.Drop.drop;
import static de.quantummaid.httpmaid.chains.rules.Jump.jumpTo;
import static de.quantummaid.httpmaid.closing.ClosingActions.CLOSING_ACTIONS;
import static de.quantummaid.httpmaid.events.EventsChains.MAP_REQUEST_TO_EVENT;
import static de.quantummaid.httpmaid.events.LoggingExceptionHandler.loggingExceptionHandler;
import static de.quantummaid.httpmaid.events.enriching.EnrichableMap.emptyEnrichableMap;
import static de.quantummaid.httpmaid.events.enriching.PerEventEnrichers.perEventEnrichers;
import static de.quantummaid.httpmaid.events.enriching.enrichers.PathParameterEnricher.pathParameterEnricher;
import static de.quantummaid.httpmaid.events.extraction.PerEventExtractors.perEventExtractors;
import static de.quantummaid.httpmaid.events.processors.BroadcastingProcessor.broadcastingProcessor;
import static de.quantummaid.httpmaid.events.processors.ConstructEventMapProcessor.constructEventMapProcessor;
import static de.quantummaid.httpmaid.events.processors.PerRequestEnrichersProcessor.enrichersProcessor;
import static de.quantummaid.httpmaid.events.processors.PerRequestExtractorsProcessor.extractorsProcessor;
import static de.quantummaid.httpmaid.events.processors.UnwrapDispatchingExceptionProcessor.unwrapDispatchingExceptionProcessor;
import static de.quantummaid.httpmaid.generator.Generator.generator;
import static de.quantummaid.httpmaid.generator.Generators.generators;
import static de.quantummaid.httpmaid.handler.distribution.HandlerDistributors.HANDLER_DISTRIBUTORS;
import static de.quantummaid.httpmaid.util.Validators.validateNotNull;
import static de.quantummaid.httpmaid.websockets.broadcast.Broadcasters.BROADCASTERS;
import static java.util.Collections.emptyList;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class EventModule implements ChainModule {
    public static final MetaDataKey<MessageBus> MESSAGE_BUS = metaDataKey("MESSAGE_BUS");
    public static final MetaDataKey<Boolean> IS_EXTERNAL_EVENT = metaDataKey("IS_EXTERNAL_EVENT");
    public static final MetaDataKey<EventType> EVENT_TYPE = metaDataKey("EVENT_TYPE");
    public static final MetaDataKey<Event> EVENT = metaDataKey("EVENT");
    public static final MetaDataKey<Optional<Object>> RECEIVED_EVENT = metaDataKey("RECEIVED_EVENT");

    private static final int DEFAULT_POOL_SIZE = 4;

    private MessageBus messageBus;
    private boolean closeMessageBusOnClose = true;
    private final List<Generator<EventType>> eventTypeGenerators = new LinkedList<>();
    private final Map<EventType, EventFactory> eventFactories = new HashMap<>();
    private final Map<EventType, PerEventEnrichers> enrichers = new HashMap<>();
    private final Map<EventType, PerEventExtractors> extractors = new HashMap<>();

    private final List<ResponseMapExtractor> responseMapExtractors = new LinkedList<>();

    public static EventModule eventModule() {
        final EventModule eventModule = new EventModule();
        final MessageBus defaultMessageBus = aMessageBus().forType(ASYNCHRONOUS)
                .withAsynchronousConfiguration(constantPoolSizeAsynchronousConfiguration(DEFAULT_POOL_SIZE))
                .withExceptionHandler(loggingExceptionHandler())
                .build();
        eventModule.setMessageBus(defaultMessageBus);
        return eventModule;
    }

    public void addEnricher(final EventType eventType, final Consumer<PerEventEnrichers> enricher) {
        enrichers.computeIfAbsent(eventType, x -> perEventEnrichers());
        final PerEventEnrichers perEventEnrichers = enrichers.get(eventType);
        enricher.accept(perEventEnrichers);
    }

    public void addExtractor(final EventType eventType, final Consumer<PerEventExtractors> extractor) {
        extractors.computeIfAbsent(eventType, x -> perEventExtractors());
        final PerEventExtractors perEventExtractors = extractors.get(eventType);
        extractor.accept(perEventExtractors);
    }

    public void setMessageBus(final MessageBus messageBus) {
        this.messageBus = messageBus;
    }

    public void setCloseMessageBusOnClose(final boolean closeMessageBusOnClose) {
        this.closeMessageBusOnClose = closeMessageBusOnClose;
    }

    public void addResponseMapExtractor(final ResponseMapExtractor extractor) {
        validateNotNull(extractor, "extractor");
        responseMapExtractors.add(extractor);
    }

    public void addEventMapping(final EventType eventType,
                                final GenerationCondition condition) {
        addEventMapping(eventType, condition, object -> emptyEnrichableMap());
        final List<String> pathParameters = condition.pathParameters();
        pathParameters.forEach(name -> {
            final PathParameterEnricher enricher = pathParameterEnricher(name, name);
            addEnricher(eventType, perEventEnrichers -> perEventEnrichers.addPathParameterEnricher(enricher));
        });
    }

    public void addEventMapping(final EventType eventType,
                                final GenerationCondition condition,
                                final EventFactory eventFactory) {
        validateNotNull(eventType, "eventType");
        validateNotNull(condition, "condition");
        validateNotNull(eventFactory, "eventFactory");
        final Generator<EventType> eventTypeGenerator = generator(eventType, condition);
        eventTypeGenerators.add(eventTypeGenerator);
        setEventFactoryFor(eventType, eventFactory);
    }

    public void setEventFactoryFor(final EventType eventType, final EventFactory eventFactory) {
        validateNotNull(eventType, "eventType");
        validateNotNull(eventFactory, "eventFactory");
        eventFactories.put(eventType, eventFactory);
    }

    @Override
    public void init(final MetaData configurationMetaData) {
        final HandlerDistributors handlerDistributors = configurationMetaData.get(HANDLER_DISTRIBUTORS);
        handlerDistributors.register(handler -> handler.handler() instanceof EventType,
                handler -> {
                    addEventMapping((EventType) handler.handler(), handler.condition());
                    return emptyList();
                });
        configurationMetaData.set(MESSAGE_BUS, messageBus);
    }

    @Override
    public void register(final ChainExtender extender) {
        extender.appendProcessor(DETERMINE_HANDLER, DetermineEventProcessor.determineEventProcessor(generators(eventTypeGenerators)));
        extender.routeIfSet(PREPARE_RESPONSE, jumpTo(MAP_REQUEST_TO_EVENT), EVENT_TYPE);

        extender.createChain(MAP_REQUEST_TO_EVENT, jumpTo(EventsChains.SUBMIT_EVENT), jumpTo(EXCEPTION_OCCURRED));
        extender.appendProcessor(MAP_REQUEST_TO_EVENT, constructEventMapProcessor(eventFactories));
        extender.appendProcessor(MAP_REQUEST_TO_EVENT, enrichersProcessor(enrichers));
        final Broadcasters broadcasters = extender.getMetaDatum(BROADCASTERS);
        extender.appendProcessor(MAP_REQUEST_TO_EVENT, broadcastingProcessor(broadcasters));

        extender.createChain(EventsChains.SUBMIT_EVENT, jumpTo(EventsChains.MAP_EVENT_TO_RESPONSE), jumpTo(EXCEPTION_OCCURRED));
        extender.appendProcessor(EventsChains.SUBMIT_EVENT, DispatchEventProcessor.dispatchEventProcessor(messageBus));

        extender.createChain(EventsChains.MAP_EVENT_TO_RESPONSE, jumpTo(POST_INVOKE), jumpTo(EXCEPTION_OCCURRED));
        responseMapExtractors.forEach(extractor -> extender.appendProcessor(EventsChains.MAP_EVENT_TO_RESPONSE, extractor));
        extender.appendProcessor(EventsChains.MAP_EVENT_TO_RESPONSE, extractorsProcessor(extractors));

        extender.appendProcessor(EventsChains.MAP_EVENT_TO_RESPONSE, metaData -> {
            final Object map = metaData.get(RECEIVED_EVENT).orElseGet(HashMap::new);
            metaData.set(RESPONSE_BODY_OBJECT, map);
        });

        extender.appendProcessor(PREPARE_EXCEPTION_RESPONSE, unwrapDispatchingExceptionProcessor());
        extender.createChain(EventsChains.EXTERNAL_EVENT, drop(), jumpTo(EXCEPTION_OCCURRED));
        extender.routeIfFlagIsSet(INIT, jumpTo(EventsChains.EXTERNAL_EVENT), IS_EXTERNAL_EVENT);

        final ChainRegistry chainRegistry = extender.getMetaDatum(CHAIN_REGISTRY);
        if (closeMessageBusOnClose) {
            final ClosingActions closingActions = chainRegistry.getMetaDatum(CLOSING_ACTIONS);
            closingActions.addClosingAction(messageBus::close);
        }
    }
}
