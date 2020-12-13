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

import de.quantummaid.httpmaid.mappath.IndexMapPathElement;
import de.quantummaid.httpmaid.mappath.statemachine.Transition;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import static de.quantummaid.httpmaid.mappath.IndexMapPathElement.indexMapPathElement;
import static de.quantummaid.httpmaid.mappath.statemachine.Transition.transitionTo;
import static de.quantummaid.httpmaid.mappath.statemachine.states.AfterIndexState.afterIndexState;
import static de.quantummaid.httpmaid.mappath.statemachine.states.ErrorState.errorState;
import static java.lang.Integer.parseInt;
import static java.lang.String.format;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class IndexState implements State {
    private final StringBuilder stringBuilder;

    public static IndexState indexState() {
        return new IndexState(new StringBuilder());
    }

    @Override
    public Transition handleOpeningSquareBrackets() {
        return transitionTo(errorState("square brackets are opened inside of square brackets"));
    }

    @Override
    public Transition handleClosingSquareBrackets() {
        if (stringBuilder.length() == 0) {
            return transitionTo(errorState("index is empty"));
        }
        final String string = stringBuilder.toString();
        final int index = parseInt(string);
        final IndexMapPathElement pathElement = indexMapPathElement(index);
        return transitionTo(afterIndexState(), pathElement);
    }

    @Override
    public Transition handleDot() {
        return handleNonNumericCharacter('.');
    }

    @Override
    public Transition handleNumericCharacter(final char c) {
        stringBuilder.append(c);
        return transitionTo(this);
    }

    @Override
    public Transition handleNonNumericCharacter(final char c) {
        return transitionTo(errorState(format("non-digit character in array index: '%s'", c)));
    }

    @Override
    public Transition handleEnd() {
        return transitionTo(errorState("square brackets are never closed"));
    }
}
