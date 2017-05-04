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

import com.atypon.wayf.dao.DbExecutor;
import com.atypon.wayf.dao.IdentityProviderDao;
import com.atypon.wayf.data.IdentityProvider;
import com.atypon.wayf.data.IdentityProviderFilter;
import com.atypon.wayf.data.cache.KeyValueCache;
import com.atypon.wayf.reactivex.DaoPolicies;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;

import java.util.HashMap;
import java.util.Map;

@Singleton
public class IdentityProviderDaoDbImpl implements IdentityProviderDao, KeyValueCache<String, String> {

    @Inject
    @Named("identity-provider.dao.db.create")
    private String createSql;

    @Inject
    @Named("identity-provider.dao.db.read")
    private String readSql;

    @Inject
    @Named("identity-provider.dao.db.filter")
    private String filterSql;

    @Inject
    private DbExecutor dbExecutor;

    public IdentityProviderDaoDbImpl() {
    }

    @Override
    public Single<IdentityProvider> create(IdentityProvider identityProvider) {
        return Single.just(identityProvider)
                .compose((single) -> DaoPolicies.applySingle(single))
                .flatMap((_identityProvider) -> dbExecutor.executeUpdate(createSql, _identityProvider))
                .flatMapMaybe((genId) -> read(identityProvider.getId()))
                .toSingle();
    }

    @Override
    public Maybe<IdentityProvider> read(String id) {
        IdentityProvider identityProvider = new IdentityProvider();
        identityProvider.setId(id);

        return Single.just(identityProvider)
                .compose((single) -> DaoPolicies.applySingle(single))
                .flatMapMaybe((_identityProvider) -> dbExecutor.executeSelectFirst(readSql, _identityProvider, IdentityProvider.class));
    }

    @Override
    public Maybe<String> get(String key) {
        return Maybe.just(key)
                .compose((maybe) -> DaoPolicies.applyMaybe(maybe))
                .map((_key) -> new IdentityProviderFilter().setEntityId(_key))
                .flatMapObservable((arguments) -> dbExecutor.executeSelect(filterSql, arguments, IdentityProvider.class))
                .singleElement()
                .map((identityProvider) -> identityProvider.getId());
    }

    @Override
    public Completable put(String key, String value) {
        return null;
    }
}
