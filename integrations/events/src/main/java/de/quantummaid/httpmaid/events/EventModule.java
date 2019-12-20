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

package de.quantummaid.httpmaid.events;

import de.quantummaid.httpmaid.events.processors.DetermineEventProcessor;
import de.quantummaid.httpmaid.events.processors.DispatchEventProcessor;
import de.quantummaid.httpmaid.events.processors.HandleExternalEventProcessor;
import de.quantummaid.httpmaid.chains.*;
import de.quantummaid.httpmaid.closing.ClosingActions;
import de.quantummaid.httpmaid.generator.GenerationCondition;
import de.quantummaid.httpmaid.generator.Generator;
import de.quantummaid.httpmaid.handler.distribution.HandlerDistributors;
import de.quantummaid.messagemaid.messageBus.MessageBus;
import de.quantummaid.messagemaid.processingContext.EventType;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.*;

import static de.quantummaid.httpmaid.HttpMaidChainKeys.RESPONSE_BODY_MAP;
import static de.quantummaid.httpmaid.HttpMaidChains.*;
import static de.quantummaid.httpmaid.chains.ChainRegistry.CHAIN_REGISTRY;
import static de.quantummaid.httpmaid.chains.MetaData.emptyMetaData;
import static de.quantummaid.httpmaid.chains.MetaDataKey.metaDataKey;
import static de.quantummaid.httpmaid.chains.rules.Drop.drop;
import static de.quantummaid.httpmaid.chains.rules.Jump.jumpTo;
import static de.quantummaid.httpmaid.closing.ClosingActions.CLOSING_ACTIONS;
import static de.quantummaid.httpmaid.events.processors.UnwrapDispatchingExceptionProcessor.unwrapDispatchingExceptionProcessor;
import static de.quantummaid.httpmaid.generator.Generator.generator;
import static de.quantummaid.httpmaid.generator.Generators.generators;
import static de.quantummaid.httpmaid.handler.distribution.HandlerDistributors.HANDLER_DISTRIBUTORS;
import static de.quantummaid.httpmaid.util.Validators.validateNotNull;
import static de.quantummaid.messagemaid.configuration.AsynchronousConfiguration.constantPoolSizeAsynchronousConfiguration;
import static de.quantummaid.messagemaid.messageBus.MessageBusBuilder.aMessageBus;
import static de.quantummaid.messagemaid.messageBus.MessageBusType.ASYNCHRONOUS;
import static java.util.Optional.of;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class EventModule implements ChainModule {
    public static final MetaDataKey<MessageBus> MESSAGE_BUS = metaDataKey("MESSAGE_BUS");
    public static final MetaDataKey<Boolean> IS_EXTERNAL_EVENT = metaDataKey("IS_EXTERNAL_EVENT");
    public static final MetaDataKey<EventType> EVENT_TYPE = metaDataKey("EVENT_TYPE");
    public static final MetaDataKey<Map<String, Object>> EVENT = metaDataKey("EVENT");
    public static final MetaDataKey<Optional<Map<String, Object>>> RECEIVED_EVENT = metaDataKey("RECEIVED_EVENT");

    private static final int DEFAULT_POOL_SIZE = 4;

    private volatile MessageBus messageBus;
    private volatile boolean closeMessageBusOnClose = true;
    private final List<Generator<EventType>> eventTypeGenerators = new LinkedList<>();

    private final List<RequestMapEnricher> requestMapEnrichers = new LinkedList<>();
    private final List<ResponseMapExtractor> responseMapExtractors = new LinkedList<>();

    private final Map<EventType, ExternalEventMapping> externalEventMappings = new HashMap<>();

    public static EventModule eventModule() {
        final EventModule eventModule = new EventModule();
        eventModule.addRequestMapEnricher((map, request) -> request.optionalBodyMap().ifPresent(map::putAll));
        final MessageBus defaultMessageBus = aMessageBus().forType(ASYNCHRONOUS)
                .withAsynchronousConfiguration(constantPoolSizeAsynchronousConfiguration(DEFAULT_POOL_SIZE))
                .build();
        eventModule.setMessageBus(defaultMessageBus);
        return eventModule;
    }

    public void setMessageBus(final MessageBus messageBus) {
        this.messageBus = messageBus;
    }

    public void setCloseMessageBusOnClose(final boolean closeMessageBusOnClose) {
        this.closeMessageBusOnClose = closeMessageBusOnClose;
    }

    public void addRequestMapEnricher(final RequestMapEnricher enricher) {
        validateNotNull(enricher, "enricher");
        requestMapEnrichers.add(enricher);
    }

    public void addResponseMapExtractor(final ResponseMapExtractor extractor) {
        validateNotNull(extractor, "extractor");
        responseMapExtractors.add(extractor);
    }

    public void addEventMapping(final EventType eventType,
                                final GenerationCondition condition) {
        validateNotNull(eventType, "eventType");
        validateNotNull(condition, "condition");
        final Generator<EventType> eventTypeGenerator = generator(eventType, condition);
        eventTypeGenerators.add(eventTypeGenerator);
    }

    public void addExternalEventMapping(final EventType eventType,
                                        final ExternalEventMapping externalEventMapping) {
        validateNotNull(eventType, "eventType");
        validateNotNull(externalEventMapping, "externalEventMapping");
        externalEventMappings.put(eventType, externalEventMapping);
    }

    @Override
    public void init(final MetaData configurationMetaData) {
        final HandlerDistributors handlerDistributors = configurationMetaData.get(HANDLER_DISTRIBUTORS);
        handlerDistributors.register(handler -> handler instanceof EventType,
                (handler, condition) -> addEventMapping((EventType) handler, condition));
        configurationMetaData.set(MESSAGE_BUS, messageBus);
    }

    @Override
    public void register(final ChainExtender extender) {
        extender.appendProcessor(DETERMINE_HANDLER, DetermineEventProcessor.determineEventProcessor(generators(eventTypeGenerators)));

        extender.routeIfSet(PREPARE_RESPONSE, jumpTo(EventsChains.MAP_REQUEST_TO_EVENT), EVENT_TYPE);

        extender.createChain(EventsChains.MAP_REQUEST_TO_EVENT, jumpTo(EventsChains.SUBMIT_EVENT), jumpTo(EXCEPTION_OCCURRED));
        extender.appendProcessor(EventsChains.MAP_REQUEST_TO_EVENT, metaData -> metaData.set(EVENT, new HashMap<>()));
        requestMapEnrichers.forEach(enricher -> extender.appendProcessor(EventsChains.MAP_REQUEST_TO_EVENT, enricher));

        extender.createChain(EventsChains.SUBMIT_EVENT, jumpTo(EventsChains.MAP_EVENT_TO_RESPONSE), jumpTo(EXCEPTION_OCCURRED));
        extender.appendProcessor(EventsChains.SUBMIT_EVENT, DispatchEventProcessor.dispatchEventProcessor(messageBus));

        extender.createChain(EventsChains.MAP_EVENT_TO_RESPONSE, jumpTo(POST_INVOKE), jumpTo(EXCEPTION_OCCURRED));
        responseMapExtractors.forEach(extractor -> extender.appendProcessor(EventsChains.MAP_EVENT_TO_RESPONSE, extractor));
        extender.appendProcessor(EventsChains.MAP_EVENT_TO_RESPONSE, metaData -> {
            final Map<String, Object> map = metaData.get(RECEIVED_EVENT).orElseGet(HashMap::new);
            metaData.set(RESPONSE_BODY_MAP, map);
        });

        extender.appendProcessor(PREPARE_EXCEPTION_RESPONSE, unwrapDispatchingExceptionProcessor());

        extender.createChain(EventsChains.EXTERNAL_EVENT, drop(), jumpTo(EXCEPTION_OCCURRED));
        extender.appendProcessor(EventsChains.EXTERNAL_EVENT, HandleExternalEventProcessor.handleExternalEventProcessor(externalEventMappings));

        extender.routeIfFlagIsSet(INIT, jumpTo(EventsChains.EXTERNAL_EVENT), IS_EXTERNAL_EVENT);

        externalEventMappings.forEach((eventType, externalEventMapping) ->
                externalEventMapping.jumpTarget().ifPresent(chainName ->
                        extender.routeIfEquals(EventsChains.EXTERNAL_EVENT, jumpTo(chainName), EVENT_TYPE, eventType)));

        final ChainRegistry chainRegistry = extender.getMetaDatum(CHAIN_REGISTRY);
        registerEventHandlers(messageBus, chainRegistry);

        if (closeMessageBusOnClose) {
            final ClosingActions closingActions = chainRegistry.getMetaDatum(CLOSING_ACTIONS);
            closingActions.addClosingAction(() -> messageBus.close());
        }
    }

    @SuppressWarnings("unchecked")
    private void registerEventHandlers(final MessageBus messageBus,
                                       final ChainRegistry chainRegistry) {
        externalEventMappings.forEach((type, mapping) -> messageBus.subscribe(type, event -> {
            final MetaData metaData = emptyMetaData();
            metaData.set(RECEIVED_EVENT, of((Map<String, Object>) event));
            metaData.set(EVENT_TYPE, type);
            metaData.set(IS_EXTERNAL_EVENT, true);
            chainRegistry.putIntoChain(INIT, metaData, m -> {
            });
        }));
    }
}
