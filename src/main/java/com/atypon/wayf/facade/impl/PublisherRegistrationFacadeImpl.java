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

import com.atypon.wayf.dao.PublisherRegistrationDao;
import com.atypon.wayf.data.InflationPolicy;
import com.atypon.wayf.data.authentication.AuthenticatedEntity;
import com.atypon.wayf.data.publisher.registration.PublisherRegistration;
import com.atypon.wayf.data.publisher.registration.PublisherRegistrationQuery;
import com.atypon.wayf.data.publisher.registration.PublisherRegistrationStatus;
import com.atypon.wayf.data.user.UserQuery;
import com.atypon.wayf.facade.PublisherRegistrationFacade;
import com.atypon.wayf.facade.UserFacade;
import com.atypon.wayf.reactivex.FacadePolicies;
import com.atypon.wayf.request.RequestContextAccessor;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Singleton
public class PublisherRegistrationFacadeImpl implements PublisherRegistrationFacade {
    private static final Logger LOG = LoggerFactory.getLogger(PublisherRegistrationFacadeImpl.class);
    private static final InflationPolicy CREATE_RESPONSE_INFLATION = new InflationPolicy().addChildPolicy(PublisherRegistrationQuery.CONTACT_FIELD, null);
    @Inject
    private UserFacade userFacade;
    @Inject
    private PublisherRegistrationDao publisherRegistrationDao;

    public PublisherRegistrationFacadeImpl() {
    }

    @Override
    public Single<PublisherRegistration> create(PublisherRegistration publisherRegistration) {
        LOG.debug("Creating publisher registration [{}]", publisherRegistration);

        publisherRegistration.setStatus(PublisherRegistrationStatus.PENDING);
        publisherRegistration.setApplicationDate(new Date());


        PublisherRegistrationQuery query = new PublisherRegistrationQuery().setInflationPolicy(CREATE_RESPONSE_INFLATION);

        return userFacade.create(publisherRegistration.getContact())
                .map((contactUser) -> {
                    publisherRegistration.setContact(contactUser);
                    return publisherRegistration;
                })
                .compose((single) -> FacadePolicies.applySingle(single))
                .flatMap((_publisherRegistration) -> publisherRegistrationDao.create(_publisherRegistration))
                .flatMap((_publisherRegistration) -> populate(query, Lists.newArrayList(_publisherRegistration)).toSingle(() -> _publisherRegistration));

    }

    @Override
    public Single<PublisherRegistration> read(PublisherRegistrationQuery query) {
        AuthenticatedEntity.authenticatedAsAdmin(RequestContextAccessor.get().getAuthenticated());

        LOG.debug("Reading publisher registration with query [{}]", query);

        return FacadePolicies.singleOrException(
                publisherRegistrationDao.read(query.getId()).compose((maybe) -> FacadePolicies.applyMaybe(maybe)),
                HttpStatus.SC_NOT_FOUND,
                "Could not read PublisherRegistration with id {}", query.getId())
                .flatMap((publisherRegistration) -> populate(query, Lists.newArrayList(publisherRegistration)).toSingle(() -> publisherRegistration));
    }

    private Completable populate(PublisherRegistrationQuery query, List<PublisherRegistration> registrations) {
        if (query.getInflationPolicy() != null && query.getInflationPolicy().getChildFields().contains(PublisherRegistrationQuery.CONTACT_FIELD)) {
            Map<Long, PublisherRegistration> registrationsByContactId = new HashMap<>();

            return Observable.fromIterable(registrations)
                    .filter((registration) -> registration.getContact() != null && registration.getContact().getId() != null)
                    .collectInto(registrationsByContactId, (map, registration) -> map.put(registration.getContact().getId(), registration))
                    .flatMapObservable((_registrationsByContactId) -> userFacade.filter(new UserQuery().ids(registrationsByContactId.keySet())))
                    .flatMapCompletable((contact) -> Completable.fromAction(() -> registrationsByContactId.get(contact.getId()).setContact(contact)));
        }

        return Completable.complete();
    }

    @Override
    public Single<PublisherRegistration> updateStatus(PublisherRegistration publisherRegistration) {
        AuthenticatedEntity.authenticatedAsAdmin(RequestContextAccessor.get().getAuthenticated());

        LOG.debug("Creating publisher registration [{}]", publisherRegistration);

        if (publisherRegistration.getStatus() == PublisherRegistrationStatus.APPROVED) {
            publisherRegistration.setApprovalDate(new Date());
        }

        return publisherRegistrationDao.update(publisherRegistration)
                .compose((single) -> FacadePolicies.applySingle(single));
    }

    @Override
    public Observable<PublisherRegistration> filter(PublisherRegistrationQuery query) {
        AuthenticatedEntity.authenticatedAsAdmin(RequestContextAccessor.get().getAuthenticated());

        LOG.debug("Filtering publisher registrations for [{}]", query);

        return publisherRegistrationDao.filter(query)
                .compose((observable) -> FacadePolicies.applyObservable(observable))
                .toList()
                .flatMapObservable((registrationList) ->
                        populate(query, registrationList)
                                .toObservable()
                                .cast(PublisherRegistration.class)
                                .concatWith(Observable.fromIterable(registrationList))
                );
    }

    public Completable delete(Long contactID) {
        if (contactID == null) {
            return Completable.complete();
        }
        return publisherRegistrationDao.delete(contactID).toCompletable();

    }

}
