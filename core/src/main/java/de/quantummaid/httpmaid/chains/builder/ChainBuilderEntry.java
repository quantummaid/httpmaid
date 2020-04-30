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

package de.quantummaid.httpmaid.chains.builder;

import de.quantummaid.httpmaid.chains.ChainName;
import de.quantummaid.httpmaid.chains.Processor;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.List;

import static de.quantummaid.httpmaid.util.Validators.validateNotNull;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
public final class ChainBuilderEntry {
    private final ChainName chainName;
    private final List<Processor> processors;

    public static ChainBuilderEntry chainBuilderEntry(final ChainName chainName,
                                                      final List<Processor> processors) {
        validateNotNull(chainName, "chainName");
        validateNotNull(processors, "processors");
        return new ChainBuilderEntry(chainName, processors);
    }

    public ChainName chainName() {
        return chainName;
    }

    public List<Processor> processors() {
        return processors;
    }
}
