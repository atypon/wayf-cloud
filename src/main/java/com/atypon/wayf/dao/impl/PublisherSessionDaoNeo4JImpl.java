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

import com.atypon.wayf.dao.PublisherSessionDao;
import com.atypon.wayf.dao.QueryMapper;
import com.atypon.wayf.dao.neo4j.Neo4JExecutor;
import com.atypon.wayf.data.publisher.PublisherSession;
import com.atypon.wayf.data.publisher.PublisherSessionQuery;
import com.atypon.wayf.reactivex.DaoPolicies;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

@Singleton
public class PublisherSessionDaoNeo4JImpl implements PublisherSessionDao {
    private static Logger LOG = LoggerFactory.getLogger(PublisherSessionDaoNeo4JImpl.class);

    @Inject
    @Named("publisher-session.dao.neo4j.create")
    private String createCypher;

    @Inject
    @Named("publisher-session.dao.neo4j.read")
    private String readCypher;

    @Inject
    @Named("publisher-session.dao.neo4j.update")
    private String updateCypher;

    @Inject
    @Named("publisher-session.dao.neo4j.delete")
    private String deleteCypher;

    @Inject
    @Named("publisher-session.dao.neo4j.add-idp-relationship")
    private String addIdpRelationshipCypher;

    @Inject
    //@Named("publisher-session.dao.neo4j.filter")
    @Named("publisher-session.dao.neo4j.filter-by-device")
    private String filterCypher;

    @Inject
    private Neo4JExecutor dbExecutor;

    public PublisherSessionDaoNeo4JImpl() {
    }

    @Override
    public Single<PublisherSession> create(PublisherSession publisherSession) {
        LOG.debug("Creating publisher session [{}] in Neo4J", publisherSession);

        publisherSession.setCreatedDate(new Date());
        publisherSession.setModifiedDate(new Date());

        return Single.just(publisherSession)
                .compose((single) -> DaoPolicies.applySingle(single))
                .map((_publisherSession) -> QueryMapper.buildQueryArguments(createCypher, _publisherSession))
                .map((arguments) -> dbExecutor.executeQuerySelectFirst(createCypher, arguments, PublisherSession.class));
    }

    @Override
    public Single<PublisherSession> read(PublisherSessionQuery query) {
        return Single.just(query)
                .compose((single) -> DaoPolicies.applySingle(single))
                .map((_query) -> QueryMapper.buildQueryArguments(readCypher, _query))
                .map((arguments) -> dbExecutor.executeQuerySelectFirst(readCypher, arguments, PublisherSession.class));
    }

    @Override
    public Single<PublisherSession> update(PublisherSession publisherSession) {
        return null;
    }

    @Override
    public Completable delete(String id) {
        return Completable.complete();
    }

    @Override
    public Completable addIdpRelationship(PublisherSession publisherSession) {
        LOG.debug("Adding IDP relationship");

        return Single.just(publisherSession)
                .compose((single) -> DaoPolicies.applySingle(single))
                .map((_publisherSession) -> QueryMapper.buildQueryArguments(addIdpRelationshipCypher, _publisherSession))
                .map((arguments) -> dbExecutor.executeQuerySelectFirst(addIdpRelationshipCypher, arguments, PublisherSession.class))
                .toCompletable();

    }

    @Override
    public Observable<PublisherSession> filter(PublisherSessionQuery filterCriteria) {
        LOG.debug("Filtering in Neo4J for criteria [{}]", filterCriteria);

        return Observable.just(filterCriteria)
                .compose((observable) -> DaoPolicies.applyObservable(observable))
                .map((_filterCriteria) -> QueryMapper.buildQueryArguments(filterCypher, filterCriteria))
                .flatMap((arguments) -> dbExecutor.executeQuery(filterCypher, arguments, PublisherSession.class));
    }
}
