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

import com.atypon.wayf.dao.DeviceDao;
import com.atypon.wayf.data.authentication.AuthenticatedEntity;
import com.atypon.wayf.data.ServiceException;
import com.atypon.wayf.data.device.Device;
import com.atypon.wayf.data.device.DeviceInfo;
import com.atypon.wayf.data.device.DeviceQuery;
import com.atypon.wayf.data.device.DeviceStatus;
import com.atypon.wayf.data.device.access.DeviceAccess;
import com.atypon.wayf.data.device.access.DeviceAccessQuery;
import com.atypon.wayf.data.device.access.DeviceAccessType;
import com.atypon.wayf.data.identity.IdentityProviderUsage;
import com.atypon.wayf.data.publisher.Publisher;
import com.atypon.wayf.facade.*;
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

import java.util.List;
import java.util.UUID;

import static com.atypon.wayf.reactivex.FacadePolicies.singleOrException;

@Singleton
public class DeviceFacadeImpl implements DeviceFacade {
    private static final Logger LOG = LoggerFactory.getLogger(DeviceFacadeImpl.class);

    @Inject
    private DeviceDao deviceDao;

    @Inject
    private DeviceAccessFacade deviceAccessFacade;

    @Inject
    private PublisherFacade publisherFacade;

    @Inject
    private IdentityProviderUsageFacade identityProviderUsageFacade;

    @Inject
    private CryptFacade cryptFacade;

    public DeviceFacadeImpl() {
    }

    @Override
    public Single<Device> create(Device device) {
        LOG.debug("Creating device [{}]", device);

        device.setStatus(DeviceStatus.ACTIVE);

        // Generate a global ID
        device.setGlobalId(UUID.randomUUID().toString());

        DeviceInfo info = device.getInfo();
        if (info == null) {
            info = new DeviceInfo();
            device.setInfo(info);
        }

        // Set the user to that of the request
        info.setUserAgent(RequestContextAccessor.get().getUserAgent());

        // Create the device
        return deviceDao.create(device);
    }

    @Override
    public Single<Device> read(DeviceQuery query) {
        LOG.debug("Reading device with query [{}]", query);

        return singleOrException(deviceDao.read(query), HttpStatus.SC_NOT_FOUND, "Invalid Global ID")
                .flatMap((device) -> populate(device, query).toSingle(() -> device));
    }

    @Override
    public Observable<Device> filter(DeviceQuery query) {
        LOG.debug("Filtering for devices that match query [{}]", query);

        return deviceDao.filter(query);
    }

    @Override
    public Single<Device> relateLocalIdToDevice(Publisher publisher, String localId) {
        return resolveFromRequest(publisher, localId)

                // Create a DeviceAccess object to store the resolved Device and Publisher. This may be
                // a little clunky but the alternative would be to create a new type to wrap the two
                .map((device) -> new DeviceAccess.Builder().device(device).publisher(publisher).type(DeviceAccessType.CREATE_IDP).build())

                .flatMap((deviceAccess) ->
                    // Now that all the required data is available, point the local ID at the device
                   deviceAccessFacade.create(deviceAccess).flatMap(deviceAccess1 -> deviceDao.updateDevicePublisherLocalIdXref(deviceAccess.getDevice().getId(), deviceAccess.getPublisher().getId(), localId)
                            .map((numAffectedRows) -> {
                                if (numAffectedRows != 1) {
                                    throw new ServiceException(HttpStatus.SC_NOT_FOUND, "Could not find local ID");
                                }

                                return deviceAccess.getDevice();
                            }))
                );
    }

    @Override
    public Completable registerLocalId(String localId) {
        return deviceDao.registerLocalId(AuthenticatedEntity.authenticatedAsPublisher(RequestContextAccessor.get().getAuthenticated()).getId(), localId);
    }

    private Single<Device> resolveFromRequest(Publisher publisher, String localId) {
        String deviceGlobalId = RequestContextAccessor.get().getDeviceId();

        LOG.debug("Found global ID from client [{}]", deviceGlobalId);
        if (deviceGlobalId != null) {
            return read(new DeviceQuery().setGlobalId(deviceGlobalId));
        }

        // If the localId has already been used, return the device associated with it. Otherwise, create a new Device
        return deviceDao.readByPublisherLocalId(publisher.getId(), localId)
                .concatWith(create(new Device()).toMaybe())
                .firstOrError();
    }

    @Override
    public Single<Device> readByLocalId(String localId) {
        LOG.debug("Reading device with local ID [{}]", localId);

        Publisher publisher = AuthenticatedEntity.authenticatedAsPublisher(RequestContextAccessor.get().getAuthenticated());

        LOG.debug("Reading device with local ID [{}] and publisher ID [{}]", localId, publisher.getId());

        return singleOrException(deviceDao.readByPublisherLocalId(publisher.getId(), localId), HttpStatus.SC_NOT_FOUND, "Invalid local ID");
    }

    private Completable populate(Device device, DeviceQuery query) {
        return Completable.mergeArray(
                inflateActivity(device, query),
                inflateHistory(device, query)
        );
    }


    private Completable inflateActivity(Device device, DeviceQuery query) {
        // Return as complete if authenticatedBy is not a requested field
        if (query.getInflationPolicy() == null || !query.getInflationPolicy().hasChildField(DeviceQuery.ACTIVITY)) {
            return Completable.complete();
        }

        return deviceAccessFacade.filter(new DeviceAccessQuery().setDeviceIds(Lists.newArrayList(device.getId())).setInflationPolicy(query.getInflationPolicy().getChildPolicy(DeviceQuery.ACTIVITY)))
                .toList()
                .flatMapCompletable((activity) -> Completable.fromAction(() -> device.setActivity(activity)));
    }

    private Completable inflateHistory(Device device, DeviceQuery query) {
        // Return as complete if authenticatedBy is not a requested field
        if (query.getInflationPolicy() == null || !query.getInflationPolicy().hasChildField(DeviceQuery.HISTORY)) {
            return Completable.complete();
        }

        return Completable.fromAction(() -> {
            List<IdentityProviderUsage> history = identityProviderUsageFacade.buildRecentHistory(device);
            device.setHistory(history);
        });
    }

    @Override
    public String encryptLocalId(Long publisherId, String localId) {
        return cryptFacade.encrypt(publisherFacade.getPublishersSalt(publisherId), localId);
    }

    @Override
    public Completable deleteDevice(Long deviceId){
        return deviceDao.delete(deviceId)
                .compose((completable) -> FacadePolicies.applyCompletable(completable))
                .andThen(deviceAccessFacade.delete(deviceId))
                .compose((completable) -> FacadePolicies.applyCompletable(completable));
    }
}