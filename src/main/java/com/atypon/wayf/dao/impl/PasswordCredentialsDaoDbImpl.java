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

package com.atypon.wayf.dao.impl;

import com.atypon.wayf.dao.PasswordCredentialsDao;
import com.atypon.wayf.data.AuthenticatedEntity;
import com.atypon.wayf.data.PasswordCredentials;
import com.atypon.wayf.database.DbExecutor;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class PasswordCredentialsDaoDbImpl implements PasswordCredentialsDao {
    private static final Logger LOG = LoggerFactory.getLogger(PasswordCredentialsDaoDbImpl.class);

    @Inject
    @Named("password-credentials.dao.db.create")
    private String createSql;

    @Inject
    @Named("password-credentials.dao.db.authenticate")
    private String authenticateSql;

    @Inject
    @Named("password-credentials.dao.db.get-salt")
    private String getSaltSql;

    @Inject
    private DbExecutor dbExecutor;

    @Override
    public Completable create(PasswordCredentials credentials) {
        LOG.debug("Creating authentication policy for [{}]", credentials);

        return dbExecutor.executeUpdate(createSql, credentials).toCompletable();
    }

    @Override
    public Maybe<AuthenticatedEntity> authenticate(PasswordCredentials credentials) {
        LOG.debug("Authenticating");

        return dbExecutor.executeSelectFirst(authenticateSql, credentials, AuthenticatedEntity.class);
    }

    @Override
    public Maybe<String> getSaltForEmail(String email) {
        LOG.debug("Getting salt for [{}]", email);

        PasswordCredentials credentials = new PasswordCredentials();
        credentials.setEmailAddress(email);

        return dbExecutor.executeSelectFirst(getSaltSql, credentials, PasswordCredentials.class)
                .map((_credentials) -> _credentials.getSalt());
    }
}
