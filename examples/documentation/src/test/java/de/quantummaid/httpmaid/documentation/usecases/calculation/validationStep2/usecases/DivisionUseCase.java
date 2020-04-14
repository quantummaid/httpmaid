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

package de.quantummaid.httpmaid.documentation.usecases.calculation.validationStep2.usecases;

import de.quantummaid.httpmaid.documentation.usecases.calculation.domain.CalculationResponse;
import de.quantummaid.httpmaid.documentation.usecases.calculation.validationStep2.domain.DivisionRequest;

import static de.quantummaid.httpmaid.documentation.usecases.calculation.domain.CalculationResponse.calculationResult;

//Showcase start divisionUsecaseStep2
public final class DivisionUseCase {

    public CalculationResponse divide(final DivisionRequest divisionRequest) {
        final int divisor = divisionRequest.divisor;
        final int result = divisionRequest.dividend / divisor;
        return calculationResult(result);
    }
}
//Showcase end divisionUsecaseStep2