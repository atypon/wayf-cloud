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

import com.atypon.wayf.dao.PublisherRegistrationDao;
import com.atypon.wayf.data.publisher.registration.PublisherRegistration;
import com.atypon.wayf.data.publisher.registration.PublisherRegistrationQuery;
import com.atypon.wayf.data.user.User;
import com.atypon.wayf.database.DbExecutor;
import com.atypon.wayf.reactivex.DaoPolicies;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.Map;

@Singleton
public class PublisherRegistrationDaoDbImpl implements PublisherRegistrationDao {
    private static final Logger LOG = LoggerFactory.getLogger(PublisherRegistrationDaoDbImpl.class);
    private static final String CONTACT_ID = "contactId";

    @Inject
    @Named("publisher-registration.dao.db.create")
    private String createSql;

    @Inject
    @Named("publisher-registration.dao.db.read")
    private String readSql;

    @Inject
    @Named("publisher-registration.dao.db.update")
    private String updateSql;

    @Inject
    @Named("publisher-registration.dao.db.delete")
    private String deleteSql;

    @Inject
    @Named("publisher-registration.dao.db.filter")
    private String filterSql;

    @Inject
    private DbExecutor dbExecutor;

    public PublisherRegistrationDaoDbImpl() {
    }

    @Override
    public Single<PublisherRegistration> create(PublisherRegistration publisherRegistration) {
        LOG.debug("Creating publisher registration [{}] in the DB", publisherRegistration);

        return Single.just(publisherRegistration)
                .compose((single) -> DaoPolicies.applySingle(single))
                .flatMap((_publisherRegistration) -> dbExecutor.executeUpdate(createSql, _publisherRegistration))
                .flatMapMaybe((genId) -> read(genId))
                .toSingle();
    }

    @Override
    public Maybe<PublisherRegistration> read(Long id) {
        LOG.debug("Reading publisher registration with id [{}] in db", id);

        PublisherRegistration publisherRegistration = new PublisherRegistration();
        publisherRegistration.setId(id);

        return Single.just(publisherRegistration)
                .compose((single) -> DaoPolicies.applySingle(single))
                .flatMapMaybe((_publisherRegistration) -> dbExecutor.executeSelectFirst(readSql, _publisherRegistration, PublisherRegistration.class));
    }

    @Override
    public Single<PublisherRegistration> update(PublisherRegistration publisherRegistration) {
        return Single.just(publisherRegistration)
                .compose((single) -> DaoPolicies.applySingle(single))
                .flatMap((_publisherRegistration) -> dbExecutor.executeUpdate(updateSql, _publisherRegistration))
                .flatMapMaybe((_ignored) -> read(publisherRegistration.getId()))
                .toSingle();
    }

    @Override
    public Observable<PublisherRegistration> filter(PublisherRegistrationQuery filter) {
        return Observable.just(filter)
                .compose((observable) -> DaoPolicies.applyObservable(observable))
                .flatMap((_filter) -> dbExecutor.executeSelect(filterSql, _filter, PublisherRegistration.class));
    }

    @Override
    public Single<Long> delete(Long contactID) {
        PublisherRegistration publisherRegistration = new PublisherRegistration();
        User user = new User();
        user.setId(contactID);
        publisherRegistration.setContact(user);
        return dbExecutor.executeUpdate(deleteSql, publisherRegistration);
    }
}
