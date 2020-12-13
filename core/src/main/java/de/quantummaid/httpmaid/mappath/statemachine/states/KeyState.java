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

package de.quantummaid.httpmaid.mappath.statemachine.states;

import de.quantummaid.httpmaid.mappath.MapPathElement;
import de.quantummaid.httpmaid.mappath.statemachine.Transition;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Optional;

import static de.quantummaid.httpmaid.mappath.KeyMapPathElement.keyMapPathElement;
import static de.quantummaid.httpmaid.mappath.statemachine.Transition.transitionTo;
import static de.quantummaid.httpmaid.mappath.statemachine.states.ErrorState.errorState;
import static de.quantummaid.httpmaid.mappath.statemachine.states.IndexState.indexState;
import static de.quantummaid.httpmaid.mappath.statemachine.states.SuccessState.successState;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class KeyState implements State {
    private final StringBuilder stringBuilder;

    public static KeyState keyState() {
        return new KeyState(new StringBuilder());
    }

    @Override
    public Transition handleOpeningSquareBrackets() {
        return buildElement()
                .map(element -> transitionTo(indexState(), element))
                .orElseGet(this::emptyKeyError);
    }

    @Override
    public Transition handleClosingSquareBrackets() {
        return transitionTo(errorState("square brackets are closed without opening them before"));
    }

    @Override
    public Transition handleDot() {
        return buildElement()
                .map(element -> transitionTo(keyState(), element))
                .orElseGet(this::emptyKeyError);
    }

    @Override
    public Transition handleNumericCharacter(final char c) {
        return handleNonNumericCharacter(c);
    }

    @Override
    public Transition handleNonNumericCharacter(final char c) {
        stringBuilder.append(c);
        return transitionTo(this);
    }

    @Override
    public Transition handleEnd() {
        return buildElement()
                .map(element -> transitionTo(successState(), element))
                .orElseGet(this::emptyKeyError);
    }

    private Optional<MapPathElement> buildElement() {
        if (stringBuilder.length() == 0) {
            return Optional.empty();
        }
        final String key = stringBuilder.toString();
        return Optional.of(keyMapPathElement(key));
    }

    private Transition emptyKeyError() {
        return transitionTo(errorState("key is empty"));
    }
}
