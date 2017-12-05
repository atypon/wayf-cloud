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

package com.atypon.wayf.dao;

import com.atypon.wayf.data.device.Device;
import com.atypon.wayf.data.device.DeviceQuery;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;

public interface DeviceDao {
    Single<Device> create(Device device);
    Maybe<Device> read(DeviceQuery query);
    Completable delete(Long id);
    Observable<Device> filter(DeviceQuery query);

    Single<Integer> updateDevicePublisherLocalIdXref(Long deviceId, Long publisherId, String localId);

    Completable registerLocalId(Long publisherId, String localId);

    Maybe<Device> readByPublisherLocalId(Long publisherId, String localId);
}
