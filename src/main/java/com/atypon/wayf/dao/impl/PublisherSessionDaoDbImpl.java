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
import com.atypon.wayf.dao.PublisherSessionDao;
import com.atypon.wayf.data.cache.KeyValueCache;
import com.atypon.wayf.data.publisher.PublisherSession;
import com.atypon.wayf.data.publisher.PublisherSessionFilter;
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

import java.util.HashMap;
import java.util.Map;

@Singleton
public class PublisherSessionDaoDbImpl implements PublisherSessionDao, KeyValueCache<String, String> {
    private static Logger LOG = LoggerFactory.getLogger(com.atypon.wayf.dao.impl.PublisherSessionDaoDbImpl.class);

    @Inject
    @Named("publisher-session.dao.db.create")
    private String createSql;

    @Inject
    @Named("publisher-session.dao.db.read")
    private String readSql;

    @Inject
    @Named("publisher-session.dao.db.update")
    private String updateSql;

    @Inject
    @Named("publisher-session.dao.db.delete")
    private String deleteSql;

    @Inject
    @Named("publisher-session.dao.db.filter")
    private String filterSql;

    @Inject
    private DbExecutor dbExecutor;

    public PublisherSessionDaoDbImpl() {
    }

    @Override
    public Single<PublisherSession> create(PublisherSession publisherSession) {
        return Single.just(publisherSession)
                .compose((single) -> DaoPolicies.applySingle(single))
                .flatMap((_publisherSession) -> dbExecutor.executeUpdate(createSql, publisherSession))
                .flatMapMaybe((genId) -> read(publisherSession.getId()))
                .toSingle();
    }

    @Override
    public Maybe<PublisherSession> read(String id) {
        PublisherSession session = new PublisherSession();
        session.setId(id);

        return Maybe.just(session)
                .compose((maybe) -> DaoPolicies.applyMaybe(maybe))
                .flatMap((_session) -> dbExecutor.executeSelectFirst(readSql, _session, PublisherSession.class));
    }

    @Override
    public Single<PublisherSession> update(PublisherSession publisherSession) {
        return Single.just(publisherSession)
                .compose((single) -> DaoPolicies.applySingle(single))
                .flatMap((_publisherSession) -> dbExecutor.executeUpdate(updateSql, publisherSession))
                .flatMapMaybe((genId) -> read(publisherSession.getId()))
                .toSingle();
    }

    @Override
    public Completable delete(String id) {
        Map<String, Object> args = new HashMap<>();
        args.put("id", id);

        return Completable.fromSingle(dbExecutor.executeUpdate(deleteSql, args))
                .compose((completable) -> DaoPolicies.applyCompletable(completable));
    }

    @Override
    public Observable<PublisherSession> filter(PublisherSessionFilter filterCriteria) {
        return Observable.just(filterCriteria)
                .compose((observable) -> DaoPolicies.applyObservable(observable))
                .flatMap((_filterCriteria) -> dbExecutor.executeSelect(filterSql, _filterCriteria, PublisherSession.class));
    }

    @Override
    public Completable put(String publisherId, String wayfId) {
        return null;
    }

    @Override
    public Maybe<String> get(String publisherId) {
        PublisherSessionFilter filter = new PublisherSessionFilter().setLocalId(publisherId);

        return Maybe.just(filter)
                .compose((maybe) -> DaoPolicies.applyMaybe(maybe))
                .flatMap((_filter) -> dbExecutor.executeSelectFirst(filterSql, _filter, PublisherSession.class))
                .map((_publisherSession) -> _publisherSession.getId());
    }
}
