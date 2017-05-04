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

import com.atypon.wayf.dao.PublisherSessionDao;
import com.atypon.wayf.data.cache.CascadingCache;
import com.atypon.wayf.data.device.Device;
import com.atypon.wayf.data.device.DeviceStatus;
import com.atypon.wayf.data.publisher.PublisherSession;
import com.atypon.wayf.data.publisher.PublisherSessionFilter;
import com.atypon.wayf.data.publisher.PublisherSessionQuery;
import com.atypon.wayf.facade.DeviceFacade;
import com.atypon.wayf.facade.IdentityProviderFacade;
import com.atypon.wayf.facade.PublisherSessionFacade;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.UUID;

@Singleton
public class PublisherSessionFacadeImpl implements PublisherSessionFacade {
    private static Logger LOG = LoggerFactory.getLogger(PublisherSessionFacadeImpl.class);

    @Inject
    private DeviceFacade deviceFacade;

    @Inject
    private PublisherSessionDao publisherSessionDao;

    @Inject
    private IdentityProviderFacade identityProviderFacade;

    @Inject
    @Named("publisherIdCache")
    private CascadingCache<String, String> idCache;

    public PublisherSessionFacadeImpl() {
    }

    @Override
    public Single<PublisherSession> create(PublisherSession publisherSession) {
            LOG.debug("Creating institution [{}]", publisherSession);

            publisherSession.setId(UUID.randomUUID().toString());
            publisherSession.setLastActiveDate(new Date());

            return Single.zip(
                            getOrCreateDevice(publisherSession.getDevice()).subscribeOn(Schedulers.io()),
                            validatePublisherIdUniqueness(publisherSession).subscribeOn(Schedulers.io()),

                            (device, isUnique) -> {
                                if (!isUnique) {
                                    throw new RuntimeException("Publisher ID must be unique");
                                }

                                LOG.debug("Setting device {}", device.getId());
                                publisherSession.setDevice(device);

                                return publisherSession;
                            }
                    )
                    .observeOn(Schedulers.io())
                    .flatMap((o_publisherSession) -> publisherSessionDao.create(o_publisherSession));
    }

    @Override
    public Single<PublisherSession> read(String id) {
        return Single.just(id)
                .observeOn(Schedulers.io())
                .flatMapMaybe((_id) -> publisherSessionDao.read(id))
                .toSingle();
    }

    @Override
    public Single<PublisherSession> update(PublisherSession publisherSession) {
        return Single.zip(
                Single.just(publisherSession),
                resolveId(publisherSession),

                (o_publisherSession, o_publisherId) -> {
                    o_publisherSession.setLocalId(o_publisherId);
                    return o_publisherSession;
                }
        );
    }

    @Override
    public Completable delete(String id) {
        return null;
    }

    @Override
    public Completable addIdpRelationship(PublisherSession publisherSession) {
        LOG.debug("Adding relationship");

        return Single.zip(
                        identityProviderFacade.resolve(publisherSession.getAuthenticatedBy()).subscribeOn(Schedulers.io()),
                        resolveId(publisherSession).subscribeOn(Schedulers.io()),

                        (identityProvider, publisherId) -> {
                            publisherSession.setAuthenticatedBy(identityProvider);
                            publisherSession.setId(publisherId);

                            return publisherSession;
                        }
                )
                .flatMap(publisherSessionToPersist -> publisherSessionDao.update(publisherSessionToPersist))
                .toCompletable();
    }

    @Override
    public Observable<PublisherSession> filter(PublisherSessionFilter filterCriteria) {
        LOG.debug("Filtering for publisher sessions with criteria [{}]", filterCriteria);

        return Observable.just(filterCriteria)
                .observeOn(Schedulers.io())
                .flatMap((_filterCriteria) -> publisherSessionDao.filter(_filterCriteria));
    }

    private Single<Device> getOrCreateDevice(Device device) {
        if (device == null) {
            device = new Device();
            device.setStatus(DeviceStatus.ACTIVE);
        }

        return Maybe.concat(
                        Observable.just(device)
                                .filter(f_device -> f_device.getId() != null)
                                .firstElement(),
                        Single.just(device)
                                .observeOn(Schedulers.io())
                                .flatMap((o_device) -> deviceFacade.create(o_device))
                                .toMaybe()
                )
                .firstOrError()
                .doOnError((e) -> {throw new RuntimeException("Unable to get or create device");});
    }

    private Single<Boolean> validatePublisherIdUniqueness(PublisherSession publisherSession) {
        // Some long running database job
        return Single.just(Boolean.TRUE);
    }


    private Single<String> resolveId(PublisherSession publisherSession) {
        Preconditions.checkArgument(publisherSession.getId() != null || publisherSession.getLocalId() != null, "PublisherSession requires an ID or Publisher ID");

        return Maybe.concat(
                        publisherSession.getId() == null ?
                                Maybe.empty() :
                                Maybe.just(publisherSession.getId()),
                        idCache.get(publisherSession.getLocalId())
                )
                .firstOrError();
    }
}
