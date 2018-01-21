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

package com.atypon.wayf.facade;

import com.atypon.wayf.data.device.Device;
import com.atypon.wayf.data.device.DeviceQuery;
import com.atypon.wayf.data.publisher.Publisher;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;

public interface DeviceFacade {
    Single<Device> create(Device device);
    Single<Device> read(DeviceQuery query);

    Single<Device> relateLocalIdToDevice(Publisher publisher, String localId);

    Observable<Device> filter(DeviceQuery query);

    Completable registerLocalId(String localId);

    Single<Device> readByLocalId(String localId);

    String encryptLocalId(Long publisherId, String localId);

    Completable deleteDevice(Long deviceId);
}
