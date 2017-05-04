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
import com.atypon.wayf.dao.DeviceDao;
import com.atypon.wayf.dao.QueryMapper;
import com.atypon.wayf.data.device.Device;
import com.atypon.wayf.reactivex.DaoPolicies;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Singleton
public class DeviceDaoDbImpl implements DeviceDao {
    private static final Logger LOG = LoggerFactory.getLogger(DeviceDaoDbImpl.class);

    @Inject
    @Named("device.dao.db.create")
    private String createSql;

    @Inject@Named("device.dao.db.read")
    private String readSql;

    @Inject
    @Named("device.dao.db.update")
    private String updateSql;

    @Inject
    @Named("device.dao.db.delete")
    private String deleteSql;

    @Inject
    private DbExecutor dbExecutor;

    public DeviceDaoDbImpl() {
    }

    @Override
    public Single<Device> create(Device device) {
        LOG.debug("Creating device [{}] in the DB", device);

        device.setId(UUID.randomUUID().toString());

        return Single.just(device)
                .compose((single) -> DaoPolicies.applySingle(single))
                .flatMap((_device) -> dbExecutor.executeUpdate(createSql, device))
                .flatMapMaybe((genId) -> read(device.getId()))
                .toSingle();
    }

    @Override
    public Maybe<Device> read(String id) {
        Device device = new Device();
        device.setId(id);

        return Single.just(device)
                .compose((single) -> DaoPolicies.applySingle(single))
                .flatMapMaybe((_device) -> dbExecutor.executeSelectFirst(readSql, _device, Device.class));
    }

    @Override
    public Single<Device> update(Device device) {
        return null;
    }

    @Override
    public Completable delete(String id) {
        Map<String, Object> args = new HashMap<>();
        args.put("id", id);

        return Completable.fromSingle(dbExecutor.executeUpdate(deleteSql, args))
                .compose((completable) -> DaoPolicies.applyCompletable(completable));    }
}
