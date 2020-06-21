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

package de.quantummaid.httpmaid.tests.unittests;

import de.quantummaid.httpmaid.util.CustomTypeValidationException;
import org.junit.jupiter.api.Test;

import static de.quantummaid.httpmaid.util.Validators.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public final class ValidatorSpecs {

    @Test
    public void canValidateNotNull() {
        CustomTypeValidationException exception = null;
        final Object object = null;
        try {
            validateNotNull(object, "object");
        } catch (final CustomTypeValidationException e) {
            exception = e;
        }
        assertThat(exception, notNullValue());
        assertThat(exception.getMessage(), is("object must not be null"));
    }

    @Test
    public void canValidateStringNotNull() {
        CustomTypeValidationException exception = null;
        final String string = null;
        try {
            validateNotNullNorEmpty(string, "string");
        } catch (final CustomTypeValidationException e) {
            exception = e;
        }
        assertThat(exception, notNullValue());
        assertThat(exception.getMessage(), is("string must not be null"));
    }

    @Test
    public void canValidateStringNotEmpty() {
        CustomTypeValidationException exception = null;
        final String string = "";
        try {
            validateNotNullNorEmpty(string, "string");
        } catch (final CustomTypeValidationException e) {
            exception = e;
        }
        assertThat(exception, notNullValue());
        assertThat(exception.getMessage(), is("string must not be empty"));
    }

    @Test
    public void canValidateStringNotOnlyWhitespace() {
        CustomTypeValidationException exception = null;
        final String string = "   ";
        try {
            validateNotNullNorEmpty(string, "string");
        } catch (final CustomTypeValidationException e) {
            exception = e;
        }
        assertThat(exception, notNullValue());
        assertThat(exception.getMessage(), is("string must not be empty"));
    }

    @Test
    public void canValidateArrayNotEmpty() {
        CustomTypeValidationException exception = null;
        final Object[] array = new Object[0];
        try {
            validateArrayNeitherNullNorEmptyNorContainsNull(array, "array");
        } catch (final CustomTypeValidationException e) {
            exception = e;
        }
        assertThat(exception, notNullValue());
        assertThat(exception.getMessage(), is("array must not be empty"));
    }
}
