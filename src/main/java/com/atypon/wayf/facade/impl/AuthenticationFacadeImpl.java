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

import com.atypon.wayf.cache.CacheManager;
import com.atypon.wayf.cache.LoadingCache;
import com.atypon.wayf.dao.AuthenticationCredentialsDao;
import com.atypon.wayf.dao.RedisDao;
import com.atypon.wayf.data.*;
import com.atypon.wayf.data.authentication.*;
import com.atypon.wayf.facade.AuthenticationFacade;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

public class AuthenticationFacadeImpl implements AuthenticationFacade {
    private static final Logger LOG = LoggerFactory.getLogger(AuthenticationFacadeImpl.class);

    @Inject
    @Named("authenticationCacheGroup")
    protected String authenticationCacheGroupName;

    @Inject
    @Named("authenticatableCache")
    protected LoadingCache<AuthenticationCredentials, AuthenticatedEntity> persistence;

    @Inject
    protected AuthenticationCredentialsDao<AuthorizationToken> authorizationTokenDao;

    @Inject
    protected AuthenticationCredentialsDao<PasswordCredentials> emailPasswordCredentialsDao;

    @Inject
    private CacheManager cacheManager;

    public <C extends AuthenticationCredentials> Single<C> createCredentials(C credentials) {
        return determineDao(credentials).create(credentials)
                .andThen(getCredentialsForAuthenticatable(credentials.getAuthenticatable()))
                .flatMapCompletable((existingCredentials) -> evictCredentials(existingCredentials))
                .andThen(Single.just(credentials));
    }

    @Override
    public Observable<AuthenticationCredentials> getCredentialsForAuthenticatable(Authenticatable authenticatable) {
        return Observable.concat(
                authorizationTokenDao.getCredentialsForAuthenticatable(authenticatable),
                emailPasswordCredentialsDao.getCredentialsForAuthenticatable(authenticatable));
    }

    @Override
    public Completable revokeCredentials(Authenticatable authenticatable) {
        return getCredentialsForAuthenticatable(authenticatable)
                .flatMapCompletable((credentials) -> revokeCredentials(credentials));
    }

    private Completable evictCredentials(AuthenticationCredentials credentials) {

        if (PasswordCredentials.class.isAssignableFrom(credentials.getClass())) {
            credentials = new CachedPasswordCredentials((PasswordCredentials) credentials);
        } else if (AuthorizationToken.class.isAssignableFrom(credentials.getClass())) {
            credentials = new CachedAuthorizationToken((AuthorizationToken) credentials);
        } else {
            throw new ServiceException(HttpStatus.SC_INTERNAL_SERVER_ERROR, "Invalid credentials type");
        }

        final AuthenticationCredentials cachedCredentails = credentials;

        return Completable.fromAction(() -> cacheManager.evictForGroup(authenticationCacheGroupName, cachedCredentails));
    }

    public Completable revokeCredentials(AuthenticationCredentials credentials) {
        return Completable.mergeArray(
                determineDao(credentials).delete(credentials),
                evictCredentials(credentials)
        );
    }

    @Override
    public AuthenticatedEntity authenticate(AuthenticationCredentials credentials) {
        LOG.debug("Authenticating credentials");

        // Use the cached version of credentials to leverage better equals and hashcode
        if (PasswordCredentials.class.isAssignableFrom(credentials.getClass())) {
            credentials = new CachedPasswordCredentials((PasswordCredentials) credentials);
        } else if (AuthorizationToken.class.isAssignableFrom(credentials.getClass())) {
            credentials = new CachedAuthorizationToken((AuthorizationToken) credentials);
        } else {
            throw new ServiceException(HttpStatus.SC_BAD_REQUEST, "Invalid authentication credentials");
        }

        try {
            AuthenticatedEntity authenticatedEntity = persistence.get(credentials).blockingGet();

            if (authenticatedEntity == null) {
                throw new ServiceException(HttpStatus.SC_UNAUTHORIZED, "Could not authenticate credentials");
            }

            if (!isStillValid(authenticatedEntity)) {
                //throw new ServiceException(HttpStatus.SC_UNAUTHORIZED, "Expired credentials");
                //The token has been expired (Ignore it)
                return null;
            }

            if (credentials instanceof CachedAuthorizationToken) {
                authenticatedEntity.setAuthenticatedUntil(((CachedAuthorizationToken) credentials).getValidUntil());
            }


            return authenticatedEntity;
        } catch (Exception e) {
            throw new ServiceException(HttpStatus.SC_UNAUTHORIZED, "Could not authenticate credentials", e);
        }
    }

    @Override
    public boolean isStillValid(AuthenticatedEntity authenticated) {
        if (authenticated.getCredentials() == null) {
            return false;
        }

        if (AuthorizationToken.class.isAssignableFrom(authenticated.getCredentials().getClass())) {
            AuthorizationToken credentials = (AuthorizationToken) authenticated.getCredentials();
            if (credentials.getValidUntil() == null) {
                return true;
            }

            return credentials.getValidUntil().compareTo(new Date()) > 0;
        }

        return true;
    }

    @Override
    public AuthenticationCredentialsDao determineDao(AuthenticationCredentials credentials) {
        if (AuthorizationToken.class.isAssignableFrom(credentials.getClass())) {
            return authorizationTokenDao;
        } else if (PasswordCredentials.class.isAssignableFrom(credentials.getClass())) {
            return emailPasswordCredentialsDao;
        }

        throw new ServiceException(HttpStatus.SC_INTERNAL_SERVER_ERROR, "Invalid credentials type");
    }
}
