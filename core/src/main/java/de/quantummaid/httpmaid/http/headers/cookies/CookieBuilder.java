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

package de.quantummaid.httpmaid.http.headers.cookies;

import de.quantummaid.httpmaid.util.Validators;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.lang.String.*;
import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.Arrays.asList;
import static java.util.Locale.US;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class CookieBuilder {
    private final DateTimeFormatter httpDateTimeFormatter = ofPattern("EEE, dd MMM yyyy HH:mm:ss O").localizedBy(US);

    private final List<String> elements;

    public static CookieBuilder cookie(final String name, final String value) {
        final CookieName cookieName = CookieName.cookieName(name);
        final CookieValue cookieValue = CookieValue.cookieValue(value);
        final String nameAndValue = format("%s=\"%s\"", cookieName.stringValue(), cookieValue.stringValue());
        final List<String> elements = new LinkedList<>();
        elements.add(nameAndValue);
        return new CookieBuilder(elements);
    }

    public CookieBuilder withAttribute(final String directive) {
        Validators.validateNotNullNorEmpty(directive, "directive");
        elements.add(directive);
        return this;
    }

    public CookieBuilder withAttribute(final String key, final String value) {
        Validators.validateNotNullNorEmpty(key, "key");
        Validators.validateNotNullNorEmpty(value, "value");
        return withAttribute(format("%s=%s", key, value));
    }

    public CookieBuilder withExpiresDirective(final String expiresDirective) {
        return withAttribute("Expires", expiresDirective);
    }

    public CookieBuilder withExpiration(final Instant expiration) {
        Validators.validateNotNull(expiration, "expiration");
        final ZonedDateTime zonedDateTime = expiration.atZone(ZoneId.of("GMT"));
        final String formattedDate = httpDateTimeFormatter.format(zonedDateTime);
        return withExpiresDirective(formattedDate);
    }

    public CookieBuilder withMaxAgeDirective(final String maxAgeDirective) {
        return withAttribute("Max-Age", maxAgeDirective);
    }

    public CookieBuilder withMaxAge(final int time, final TimeUnit unit) {
        Validators.validateNotNull(unit, "unit");
        final long maxAgeInSeconds = unit.toSeconds(time);
        return withMaxAgeDirective(valueOf(maxAgeInSeconds));
    }

    public CookieBuilder withSecureDirective() {
        return withAttribute("Secure");
    }

    public CookieBuilder thatIsOnlySentViaHttps() {
        return withSecureDirective();
    }

    public CookieBuilder withHttpOnlyDirective() {
        return withAttribute("HttpOnly");
    }

    public CookieBuilder thatIsNotAccessibleFromJavaScript() {
        return withHttpOnlyDirective();
    }

    public CookieBuilder withSameSiteDirective(final String sameSiteDirective) {
        return withAttribute("SameSite", sameSiteDirective);
    }

    public CookieBuilder withSameSitePolicy(final SameSitePolicy policy) {
        Validators.validateNotNull(policy, "policy");
        return withSameSiteDirective(policy.stringValue());
    }

    public CookieBuilder withDomainDirective(final String domainDirective) {
        return withAttribute("Domain", domainDirective);
    }

    public CookieBuilder exposedToAllSubdomainsOf(final String... domains) {
        return withDomainDirective(join(",", asList(domains)));
    }

    public CookieBuilder withPathDirective(final String pathDirective) {
        return withAttribute("Path", pathDirective);
    }

    public CookieBuilder exposedOnlyToSubpathsOf(final String... paths) {
        return withPathDirective(join(",", asList(paths)));
    }

    public String build() {
        return join("; ", elements);
    }
}
