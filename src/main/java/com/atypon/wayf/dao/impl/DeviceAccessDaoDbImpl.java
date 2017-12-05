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

import com.atypon.wayf.dao.DeviceAccessDao;
import com.atypon.wayf.data.device.Device;
import com.atypon.wayf.data.device.access.DeviceAccess;
import com.atypon.wayf.data.device.access.DeviceAccessQuery;
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

@Singleton
public class DeviceAccessDaoDbImpl implements DeviceAccessDao {
    private static Logger LOG = LoggerFactory.getLogger(com.atypon.wayf.dao.impl.DeviceAccessDaoDbImpl.class);

    @Inject
    @Named("device-access.dao.db.create")
    private String createSql;

    @Inject
    @Named("device-access.dao.db.read")
    private String readSql;

    @Inject
    @Named("device-access.dao.db.filter")
    private String filterSql;

    @Inject
    @Named("device-access.dao.db.delete")
    private String deleteSql;

    @Inject
    private DbExecutor dbExecutor;

    public DeviceAccessDaoDbImpl() {
    }

    @Override
    public Single<DeviceAccess> create(DeviceAccess deviceAccess) {
        return Single.just(deviceAccess)
                .compose((single) -> DaoPolicies.applySingle(single))
                .flatMap((_deviceAccess) -> dbExecutor.executeUpdate(createSql, deviceAccess))
                .flatMapMaybe((genId) -> read(genId))
                .toSingle();
    }

    @Override
    public Maybe<DeviceAccess> read(Long id) {
        DeviceAccess session = new DeviceAccess();
        session.setId(id);

        return Maybe.just(session)
                .compose((maybe) -> DaoPolicies.applyMaybe(maybe))
                .flatMap((_session) -> dbExecutor.executeSelectFirst(readSql, _session, DeviceAccess.class));
    }

    @Override
    public Observable<DeviceAccess> filter(DeviceAccessQuery query) {
        return Observable.just(query)
                .compose((observable) -> DaoPolicies.applyObservable(observable))
                .flatMap((_query) -> dbExecutor.executeSelect(filterSql, _query, DeviceAccess.class));
    }

    @Override
    public Completable delete(Long deviceId){
        Device device = new Device();
        device.setId(deviceId);
        DeviceAccess deviceAccess = new DeviceAccess();
        deviceAccess.setDevice(device);
        return dbExecutor.executeUpdate(deleteSql, deviceAccess).toCompletable();
    }

}
