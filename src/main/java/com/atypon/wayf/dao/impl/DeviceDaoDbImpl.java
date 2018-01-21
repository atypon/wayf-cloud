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

import com.atypon.wayf.dao.DeviceDao;
import com.atypon.wayf.data.device.Device;
import com.atypon.wayf.data.device.DeviceQuery;
import com.atypon.wayf.database.DbExecutor;
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
public class DeviceDaoDbImpl implements DeviceDao {
    private static final Logger LOG = LoggerFactory.getLogger(DeviceDaoDbImpl.class);

    private static final String DEVICE_ID = "device.id";
    private static final String PUBLISHER_ID = "publisher.id";
    private static final String PUBLISHER_LOCAL_ID = "localId";
    private static final String UNIQUE_PUBLISHER_KEY = "uniquePublisherKey";

    @Inject
    @Named("device.dao.db.create")
    private String createSql;

    @Inject@Named("device.dao.db.read")
    private String readSql;

    @Inject
    @Named("device.dao.db.delete")
    private String deleteSql;

    @Inject
    @Named("device.dao.db.filter")
    private String filterSql;

    @Inject
    @Named("device.dao.db.update-publisher-local-id-xref")
    private String replacePublisherLocalIdXrefSql;

    @Inject
    @Named("device.dao.db.register-local-id-xref")
    private String registerLocalIdSql;

    @Inject
    @Named("device.dao.db.read-by-publisher-local-id")
    private String readByPublisherLocalIdSql;

    @Inject
    @Named("device.dao.db.delete-publisher-local-id-xref")
    private String deletePublisherLocalIdXrefSql;

    @Inject
    private DbExecutor dbExecutor;

    public DeviceDaoDbImpl() {
    }

    @Override
    public Single<Device> create(Device device) {
        LOG.debug("Creating device [{}] in the DB", device);

        return Single.just(device)
                .compose((single) -> DaoPolicies.applySingle(single))
                .flatMap((_device) -> dbExecutor.executeUpdate(createSql, device))
                .flatMapMaybe((genId) -> read(new DeviceQuery().setGlobalId(device.getGlobalId())))
                .toSingle();
    }

    @Override
    public Maybe<Device> read(DeviceQuery query) {
        return Single.just(query)
                .compose((single) -> DaoPolicies.applySingle(single))
                .flatMapMaybe((_query) -> dbExecutor.executeSelectFirst(readSql, _query, Device.class));
    }

    @Override
    public Completable delete(Long id) {
        Device device = new Device();
        device.setId(id);
        return dbExecutor.executeUpdate(deleteSql, device)
                .toCompletable()
                .andThen(dbExecutor.executeUpdate(deletePublisherLocalIdXrefSql, device))
                .toCompletable();

    }

    @Override
    public Observable<Device> filter(DeviceQuery query) {
        return Single.just(query)
                .compose((single) -> DaoPolicies.applySingle(single))
                .flatMapObservable((_query) -> dbExecutor.executeSelect(filterSql, _query, Device.class));
    }

    @Override
    public Single<Integer> updateDevicePublisherLocalIdXref(Long deviceId, Long publisherId, String localId) {
        Map<String, Object> args = new HashMap<>();
        args.put(DEVICE_ID, deviceId);
        args.put(PUBLISHER_ID, publisherId);
        args.put(PUBLISHER_LOCAL_ID, localId);
        args.put(UNIQUE_PUBLISHER_KEY, publisherId + "-" + localId);

        return dbExecutor.executeUpdateRowCount(replacePublisherLocalIdXrefSql, args)
                .compose((single) -> DaoPolicies.applySingle(single));
    }

    @Override
    public Completable registerLocalId(Long publisherId, String localId) {
        Map<String, Object> args = new HashMap<>();
        args.put(PUBLISHER_ID, publisherId);
        args.put(PUBLISHER_LOCAL_ID, localId);
        args.put(UNIQUE_PUBLISHER_KEY, publisherId + "-" + localId);

        return Completable.fromSingle(dbExecutor.executeUpdate(registerLocalIdSql, args))
                .compose((completable) -> DaoPolicies.applyCompletable(completable));
    }


    @Override
    public Maybe<Device> readByPublisherLocalId(Long publisherId, String localId) {
        Map<String, Object> args = new HashMap<>();
        args.put(PUBLISHER_ID, publisherId);
        args.put(PUBLISHER_LOCAL_ID, localId);

        return Maybe.just(args)
                .compose((maybe) -> DaoPolicies.applyMaybe(maybe))
                .flatMap((_args) -> dbExecutor.executeSelectFirst(readByPublisherLocalIdSql, args, Device.class));
    }
}
