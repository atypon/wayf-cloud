/*
 * Copyright 2017 Atypon Systems, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.atypon.wayf.data.authentication;

import com.atypon.wayf.data.ServiceException;
import com.atypon.wayf.data.publisher.Publisher;
import com.atypon.wayf.data.user.User;
import org.apache.http.HttpStatus;

import java.util.Date;

public class AuthenticatedEntity {
    private Authenticatable authenticatable;
    private AuthenticationCredentials credentials;
    private Date authenticatedUntil;

    public Authenticatable getAuthenticatable() {
        return authenticatable;
    }

    public void setAuthenticatable(Authenticatable authenticatable) {
        this.authenticatable = authenticatable;
    }

    public Date getAuthenticatedUntil() {
        return authenticatedUntil;
    }

    public void setAuthenticatedUntil(Date authenticatedUntil) {
        this.authenticatedUntil = authenticatedUntil;
    }

    public AuthenticationCredentials getCredentials() {
        return credentials;
    }

    public void setCredentials(AuthenticationCredentials credentials) {
        this.credentials = credentials;
    }

    public static Publisher authenticatedAsPublisher(AuthenticatedEntity authenticatable) {
        if (authenticatable != null
                && authenticatable.getAuthenticatable() != null
                && Publisher.class.isAssignableFrom(authenticatable.getAuthenticatable().getClass()) && stillValid(authenticatable.getAuthenticatedUntil())) {
            return (Publisher) authenticatable.getAuthenticatable();
        }

        throw new ServiceException(HttpStatus.SC_UNAUTHORIZED, "An authenticated Publisher is required");
    }

    public static User authenticatedAsAdmin(AuthenticatedEntity authenticatable) {
        if (authenticatable != null
                && authenticatable.getAuthenticatable() != null
                && User.class.isAssignableFrom(authenticatable.getAuthenticatable().getClass()) && stillValid(authenticatable.getAuthenticatedUntil())) {
            return (User) authenticatable.getAuthenticatable();
        }

        throw new ServiceException(HttpStatus.SC_UNAUTHORIZED, "An authenticated Administrator is required");
    }

    private static boolean stillValid(Date validUntil) {
        return validUntil == null || validUntil.compareTo(new Date()) > 0;
    }
}
