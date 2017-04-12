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
import com.atypon.wayf.dao.neo4j.Neo4JExecutor;
import com.atypon.wayf.data.IdentityProvider;
import com.atypon.wayf.data.cache.KeyValueCache;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Singleton
public class IdentityProviderDaoNeo4JImpl implements IdentityProviderDao, KeyValueCache<String, String> {

    @Inject
    @Named("identity-provider.dao.neo4j.create")
    private String createCypher;

    @Inject
    @Named("identity-provider.dao.neo4j.get-by-entity-id")
    private String getByEntityIdCypher;

    public IdentityProviderDaoNeo4JImpl() {
    }

    @Override
    public Single<IdentityProvider> create(IdentityProvider identityProvider) {
        return Single.just(identityProvider)
                .observeOn(Schedulers.io())
                .map((identityProviderToWrite) -> {
                    Map<String, Object> args = new HashMap<>();
                    args.put("id", identityProvider.getId());
                    args.put("name", identityProvider.getName());
                    args.put("entityId", identityProvider.getEntityId());
                    args.put("createdDate", identityProvider.getCreatedDate().getTime());
                    args.put("modifiedDate", identityProvider.getModifiedDate().getTime());

                    return Neo4JExecutor.executeQuery(createCypher, args, IdentityProvider.class).get(0);
                });
    }

    @Override
    public Maybe<String> get(String key) {
        return Maybe.just(key)
                .observeOn(Schedulers.io())
                .flatMap((keyToRead) -> {
                    Map<String, Object> args = new HashMap<>();
                    args.put("entityId", key);


                    List<IdentityProvider> results = Neo4JExecutor.executeQuery(getByEntityIdCypher, args, IdentityProvider.class);


                    return results.size() == 0? Maybe.empty() : Maybe.just(results.get(0).getId());
                });
    }

    @Override
    public Completable put(String key, String value) {
        return null;
    }
}