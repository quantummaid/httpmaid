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

package de.quantummaid.httpmaid.documentation.xx_usecases.calculation.validationStep2.domain;

//Showcase start divisionRequestStep2
public final class DivisionRequest {
    public final Integer dividend;
    public final Integer divisor;

    public DivisionRequest(final Integer dividend, final Integer divisor) {
        this.dividend = dividend;
        this.divisor = divisor;
    }

    public static DivisionRequest divisionRequest(final Integer dividend,
                                                  final Integer divisor) {
        if (divisor == 0) {
            throw new IllegalArgumentException("the divisor must not be 0");
        }
        return new DivisionRequest(dividend, divisor);
    }
}
//Showcase end divisionRequestStep2
