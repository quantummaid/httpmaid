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

package de.quantummaid.httpmaid.util.describing;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.quantummaid.httpmaid.util.Validators.validateNotNull;
import static de.quantummaid.httpmaid.util.Validators.validateNotNullNorEmpty;
import static de.quantummaid.httpmaid.util.describing.Dimensions.calculateDimensions;
import static de.quantummaid.httpmaid.util.describing.EscapedText.escapedText;
import static de.quantummaid.httpmaid.util.describing.TextCanvas.textCanvas;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class CardBuilder {
    private String headline;
    private final Map<String, EscapedText> entries = new HashMap<>();

    static CardBuilder cardBuilder() {
        return new CardBuilder();
    }

    void setHeadline(final String headline) {
        validateNotNullNorEmpty(headline, "headline");
        this.headline = headline;
    }

    void addEntry(final String key, final String value) {
        validateNotNullNorEmpty(key, "key");
        validateNotNull(value, "value");
        final EscapedText escapedValue = escapedText(value);
        entries.put(key, escapedValue);
    }

    public String build() {
        final Dimensions dimensions = calculateDimensions(entries);
        final TextCanvas canvas = textCanvas(dimensions);
        canvas.draw(thickHorizontalLine(dimensions));
        final String centeredTitle = frame(DescriptionUtils.center(headline, dimensions.width() - 2));
        canvas.draw(centeredTitle);
        canvas.draw(thickHorizontalLine(dimensions));

        entries.forEach((key, value) -> {
            canvas.draw(keyValue(key, value, dimensions));
            canvas.draw(thinHorizontalLine(dimensions));
        });

        return canvas.build();
    }

    private static String keyValue(final String key,
                                   final EscapedText value,
                                   final Dimensions dimensions) {
        final StringBuilder builder = new StringBuilder();
        final List<String> renderedLines = value.renderLines(dimensions.lastRowLength());
        builder.append(keyValueLine(key, firstElement(renderedLines, ""), dimensions));
        for (int i = 1; i < renderedLines.size(); ++i) {
            builder.append("\n");
            final String line = renderedLines.get(i);
            builder.append(keyValueLine("", line, dimensions));
        }
        return builder.toString();
    }

    private static String keyValueLine(final String key,
                                       final String value,
                                       final Dimensions dimensions) {
        final StringBuilder builder = new StringBuilder();
        builder.append(" ");
        builder.append(DescriptionUtils.pad(key, dimensions.firstRowLength()));
        builder.append(" | ");
        builder.append(value);
        return frame(DescriptionUtils.pad(builder.toString(), dimensions.width() - 2));
    }

    private String thickHorizontalLine(final Dimensions dimensions) {
        return horizontalLine(DescriptionUtils.HORIZONTAL_THICK, dimensions);
    }

    private String thinHorizontalLine(final Dimensions dimensions) {
        return horizontalLine(DescriptionUtils.HORIZONTAL_THIN, dimensions);
    }

    private String horizontalLine(final String character, final Dimensions dimensions) {
        final int innerLength = dimensions.width() - 2;
        return frame(DescriptionUtils.times(character, innerLength));
    }

    private static String frame(final String string) {
        return String.format("%s%s%s", DescriptionUtils.VERTICAL, string, DescriptionUtils.VERTICAL);
    }

    private static <T> T firstElement(final List<T> list, final T alternative) {
        if (list.isEmpty()) {
            return alternative;
        }
        return list.get(0);
    }
}
