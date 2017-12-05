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

import com.atypon.wayf.dao.UserDao;
import com.atypon.wayf.data.ServiceException;
import com.atypon.wayf.data.authentication.AuthenticatedEntity;
import com.atypon.wayf.data.authentication.PasswordCredentials;
import com.atypon.wayf.data.user.User;
import com.atypon.wayf.data.user.UserQuery;
import com.atypon.wayf.facade.*;
import com.atypon.wayf.reactivex.FacadePolicies;
import com.atypon.wayf.request.RequestContextAccessor;
import com.google.inject.Inject;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserFacadeImpl implements UserFacade {
    private static final Logger LOG = LoggerFactory.getLogger(UserFacadeImpl.class);

    @Inject
    private UserDao dao;

    @Inject
    private AuthenticationFacade authenticationFacade;

    @Inject
    private PasswordCredentialsFacade passwordCredentialsFacade;

    @Override
    public Single<User> create(User user) {
        LOG.debug("Creating user [{}]", user);

        return dao.create(user)
                .compose((single) -> FacadePolicies.applySingle(single))
                .map((createdUser) -> {
                            createdUser.setCredentials(user.getCredentials());
                            return createdUser;
                        }
                )
                .flatMap((createdUser) ->
                        Maybe.zip(
                                Maybe.just(createdUser),
                                generateEmailCredentials(createdUser),

                                (_user, credentials) -> _user
                        ).toSingle(createdUser)
                );
    }

    @Override
    public Single<User> read(Long id) {
        LOG.debug("Reading user with ID [{}]", id);

        return FacadePolicies.singleOrException(
                dao.read(id).
                        compose((maybe) -> FacadePolicies.applyMaybe(maybe)),
                HttpStatus.SC_NOT_FOUND,
                "Could not read User with id {}", id);
    }

    @Override
    public Observable<User> filter(UserQuery query) {
        LOG.debug("Filtering users for [{}]", query);

        boolean isAdminView = UserQuery.ADMIN_VIEW.equals(query.getView());

        if (isAdminView) {
            AuthenticatedEntity.authenticatedAsAdmin(RequestContextAccessor.get().getAuthenticated());
        }

        return isAdminView ?
                dao.adminFilter(query).compose((observable) -> FacadePolicies.applyObservable(observable)) :
                dao.filter(query).compose((observable) -> FacadePolicies.applyObservable(observable));
    }

    @Override
    public Completable delete(Long id) {
        if (id == null) {
            return Completable.complete();
        }
        User adminUser = AuthenticatedEntity.authenticatedAsAdmin(RequestContextAccessor.get().getAuthenticated());
        if (adminUser.getId().equals(id)) {
            throw new ServiceException(HttpStatus.SC_BAD_REQUEST, "User may not delete themselves");
        }
        User userToDelete = new User();
        userToDelete.setId(id);
        return dao.delete(id)
                .compose((completable) -> FacadePolicies.applyCompletable(completable))
                .andThen(authenticationFacade.revokeCredentials(userToDelete));
    }


    private Maybe<PasswordCredentials> generateEmailCredentials(User user) {
        if (user.getCredentials() != null) {
            return passwordCredentialsFacade.generateEmailCredentials(user).toMaybe();
        }

        return Maybe.empty();
    }
}
