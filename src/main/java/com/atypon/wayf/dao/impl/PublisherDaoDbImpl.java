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

import com.atypon.wayf.dao.PublisherDao;
import com.atypon.wayf.dao.DbExecutor;
import com.atypon.wayf.data.publisher.Publisher;
import com.atypon.wayf.data.publisher.PublisherFilter;
import com.atypon.wayf.reactivex.DaoPolicies;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Singleton
public class PublisherDaoDbImpl implements PublisherDao {
    private static final Logger LOG = LoggerFactory.getLogger(PublisherDaoDbImpl.class);

    @Inject
    @Named("publisher.dao.db.create")
    private String createSql;

    @Inject
    @Named("publisher.dao.db.read")
    private String readSql;

    @Inject
    @Named("publisher.dao.db.update")
    private String updateSql;

    @Inject
    @Named("publisher.dao.db.delete")
    private String deleteSql;

    @Inject
    @Named("publisher.dao.db.filter")
    private String filterSql;

    @Inject
    private DbExecutor dbExecutor;

    public PublisherDaoDbImpl() {
    }

    @Override
    public Single<Publisher> create(Publisher publisher) {
        LOG.debug("Creating publisher [{}] in the DB", publisher);

        publisher.setId(UUID.randomUUID().toString());
        publisher.setCreatedDate(new Date());
        publisher.setModifiedDate(new Date());

        return Single.just(publisher)
                .compose((single) -> DaoPolicies.applySingle(single))
                .flatMap((_publisher) -> dbExecutor.executeUpdate(createSql, _publisher))
                .flatMapMaybe((genId) -> read(publisher.getId()))
                .toSingle();
    }

    @Override
    public Maybe<Publisher> read(String id) {
        LOG.debug("Reading publisher with id [{}] in Neo4J", id);

        Publisher publisher = new Publisher();
        publisher.setId(id);

        return Single.just(publisher)
                .compose((single) -> DaoPolicies.applySingle(single))
                .flatMapMaybe((_publisher) -> dbExecutor.executeSelectFirst(readSql, _publisher, Publisher.class));
    }

    @Override
    public Single<Publisher> update(Publisher publisher) {
        return null;
    }

    @Override
    public Completable delete(String id) {
        Map<String, Object> args = new HashMap<>();
        args.put("id", id);

        return Completable.fromSingle(dbExecutor.executeUpdate(deleteSql, args))
                .compose((completable) -> DaoPolicies.applyCompletable(completable));
    }

    @Override
    public Observable<Publisher> filter(PublisherFilter filter) {
        return Observable.just(filter)
                .compose((observable) -> DaoPolicies.applyObservable(observable))
                .flatMap((_filter) -> dbExecutor.executeSelect(filterSql, _filter, Publisher.class));
    }
}
