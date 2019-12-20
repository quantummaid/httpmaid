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

package de.quantummaid.httpmaid.security.authorization;

import de.quantummaid.httpmaid.chains.MetaData;

public final class NotAuthorizedException extends RuntimeException {
    private final AuthorizerId authorizerId;

    private NotAuthorizedException(final String message, final AuthorizerId authorizerId) {
        super(message);
        this.authorizerId = authorizerId;
    }

    public static NotAuthorizedException notAuthorizedException(final MetaData metaData, final AuthorizerId authorizerId) {
        return new NotAuthorizedException("Not authorized to access: " + metaData, authorizerId);
    }

    public AuthorizerId authorizerId() {
        return authorizerId;
    }
}
