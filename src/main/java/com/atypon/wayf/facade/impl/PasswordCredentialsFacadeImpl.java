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

package com.atypon.wayf.facade.impl;

import com.atypon.wayf.cache.Cache;
import com.atypon.wayf.cache.CacheManager;
import com.atypon.wayf.dao.PasswordCredentialsDao;
import com.atypon.wayf.data.ServiceException;
import com.atypon.wayf.data.authentication.AuthenticatedEntity;
import com.atypon.wayf.data.authentication.AuthorizationToken;
import com.atypon.wayf.data.authentication.PasswordCredentials;
import com.atypon.wayf.data.user.User;
import com.atypon.wayf.facade.*;
import com.atypon.wayf.reactivex.FacadePolicies;
import com.atypon.wayf.request.RequestContextAccessor;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import org.apache.http.HttpStatus;

@Singleton
public class PasswordCredentialsFacadeImpl implements PasswordCredentialsFacade {
    private static final Long ADMIN_TOKEN_LIFESPAN = 7200000L; // 2 hours

    @Inject
    private PasswordCredentialsDao credentialsDao;

    @Inject
    private CryptFacade cryptFacade;

    @Inject
    @Named("passwordSaltCache")
    private Cache<String, String> saltCache;

    @Inject
    private AuthenticationFacade authenticationFacade;

    @Inject
    private AuthorizationTokenFactory authorizationTokenFactory;

    @Inject
    @Named("passwordSaltCacheGroup")
    private String passwordSaltCacheGroup;

    @Inject
    private CacheManager cacheManager;

    @Override
    public Single<String> getSaltForEmail(String email) {
        return FacadePolicies.singleOrException(credentialsDao.getSaltForEmail(email), HttpStatus.SC_BAD_REQUEST, "Invalid email address");
    }

    @Override
    public Completable resetPassword(Long userId, PasswordCredentials credentials) {
        AuthenticatedEntity.authenticatedAsAdmin(RequestContextAccessor.get().getAuthenticated());

        User user = new User();
        user.setId(userId);

        return FacadePolicies.singleOrException(
                    authenticationFacade.getCredentialsForAuthenticatable(user)
                        .filter((userCredentials) -> PasswordCredentials.class.isAssignableFrom(userCredentials.getClass())),
                    HttpStatus.SC_INTERNAL_SERVER_ERROR,
                    "Could not determine user's login credentials")

                // Update the password to the new value
                .flatMap((passwordCredentials) -> {
                        // Invalidate the salt caches
                        cacheManager.evictForGroup(passwordSaltCacheGroup, ((PasswordCredentials) passwordCredentials).getEmailAddress());

                        credentials.setEmailAddress(((PasswordCredentials) passwordCredentials).getEmailAddress()); // Copy over the email address
                        user.setCredentials(credentials);

                        return authenticationFacade.revokeCredentials(user) // Revoke all existing credentials
                                .andThen(generateEmailCredentials(user)); // Create the new credentials

                }).toCompletable();
    }

    @Override
    public Single<AuthorizationToken> generateSessionToken(PasswordCredentials credentials) {
        if (credentials.getEmailAddress() == null) {
            throw new ServiceException(HttpStatus.SC_BAD_REQUEST, "Email Address is required to login");
        }

        return FacadePolicies.singleOrException(saltCache.get(credentials.getEmailAddress()), HttpStatus.SC_UNAUTHORIZED, "Invalid credentials")
                .map((salt) -> cryptFacade.encrypt(salt, credentials.getPassword()))
                .map((encryptedPassword) -> {
                        credentials.setPassword(encryptedPassword);
                        return authenticationFacade.authenticate(credentials);
                    }
                )
                .flatMap((authenticatedEntity) -> {
                        AuthorizationToken token = authorizationTokenFactory.generateExpiringToken(authenticatedEntity.getAuthenticatable(), ADMIN_TOKEN_LIFESPAN);
                        return authenticationFacade.createCredentials(token);
                });
    }

    @Override
    public Single<PasswordCredentials> generateEmailCredentials(User user) {
        AuthenticatedEntity.authenticatedAsAdmin(RequestContextAccessor.get().getAuthenticated());

        PasswordCredentials credentials = user.getCredentials();

        String salt = cryptFacade.generateSalt();
        String encryptedPassword = cryptFacade.encrypt(salt, credentials.getPassword());

        credentials.setSalt(salt);
        credentials.setPassword(encryptedPassword);
        credentials.setAuthenticatable(user);

        return authenticationFacade.createCredentials(credentials);
    }

    public Observable<PasswordCredentials> getAllAdminEmails(){
        return credentialsDao.getAllEmails();
    }
}
