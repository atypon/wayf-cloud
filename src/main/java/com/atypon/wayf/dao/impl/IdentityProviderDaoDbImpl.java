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

import com.atypon.wayf.dao.IdentityProviderDao;
import com.atypon.wayf.data.identity.IdentityProvider;
import com.atypon.wayf.data.identity.IdentityProviderQuery;
import com.atypon.wayf.database.DbExecutor;
import com.atypon.wayf.reactivex.DaoPolicies;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;

public class IdentityProviderDaoDbImpl implements IdentityProviderDao {

    private String createSql;
    private String readSql;
    private String filterSql;
    private DbExecutor dbExecutor;
    private Class<? extends IdentityProvider> resultClass;

    public IdentityProviderDaoDbImpl(String createSql, String readSql, String filterSql, DbExecutor dbExecutor, Class<? extends IdentityProvider> resultClass) {
        this.createSql = createSql;
        this.readSql = readSql;
        this.filterSql = filterSql;
        this.dbExecutor = dbExecutor;
        this.resultClass = resultClass;
    }

    @Override
    public Single<IdentityProvider> create(IdentityProvider identityProvider) {
        return Single.just(identityProvider)
                .compose(DaoPolicies::applySingle)
                .flatMap((_identityProvider) -> dbExecutor.executeUpdate(createSql, _identityProvider))
                .flatMapMaybe(this::read)
                .toSingle();
    }

    @Override
    public Maybe<IdentityProvider> read(Long id) {
        IdentityProviderQuery query = new IdentityProviderQuery().setId(id);

        return Single.just(query)
                .compose(DaoPolicies::applySingle)
                .flatMapMaybe((_query) -> dbExecutor.executeSelectFirst(readSql, _query, resultClass));
    }

    @Override
    public Observable<IdentityProvider> filter(IdentityProviderQuery query) {
        return Single.just(query)
                .compose(DaoPolicies::applySingle)
                .flatMapObservable((_query) -> dbExecutor.executeSelect(filterSql, query, resultClass));
    }
}
