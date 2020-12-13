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

package de.quantummaid.httpmaid.mappath.statemachine;

import de.quantummaid.httpmaid.mappath.MapPathElement;
import de.quantummaid.httpmaid.mappath.statemachine.states.ErrorState;
import de.quantummaid.httpmaid.mappath.statemachine.states.State;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import static de.quantummaid.httpmaid.mappath.MapPathException.parseException;
import static de.quantummaid.httpmaid.mappath.statemachine.states.KeyState.keyState;
import static java.lang.Character.isDigit;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class StateMachine {
    private final String input;
    private State state = keyState();
    private final List<MapPathElement> elements = new ArrayList<>();

    public static List<MapPathElement> runStateMachineOn(final String input) {
        final StateMachine stateMachine = new StateMachine(input);
        stateMachine.run();
        return stateMachine.elements;
    }

    private void run() {
        final char[] chars = input.toCharArray();
        int index;
        for (index = 0; index < chars.length; ++index) {
            final Character c = chars[index];
            final Transition transition = dispatch(c);
            handleTransition(transition, index);
        }
        final Transition endTransition = state.handleEnd();
        handleTransition(endTransition, index);
    }

    private Transition dispatch(final Character c) {
        if (c == null) {
            return state.handleEnd();
        }
        if (c == '[') {
            return state.handleOpeningSquareBrackets();
        }
        if (c == ']') {
            return state.handleClosingSquareBrackets();
        }
        if (c == '.') {
            return state.handleDot();
        }
        if (isDigit(c)) {
            return state.handleNumericCharacter(c);
        }
        if (c == '\n') {
            return state.handleNewline();
        }
        return state.handleNonNumericCharacter(c);
    }

    private void handleTransition(final Transition transition,
                                  final int index) {
        transition.mapPathElement().ifPresent(elements::add);
        state = transition.nextState();

        if (state instanceof ErrorState) {
            final ErrorState errorState = (ErrorState) state;
            final String message = errorState.message();
            throw parseException(message, input, index);
        }
    }
}
