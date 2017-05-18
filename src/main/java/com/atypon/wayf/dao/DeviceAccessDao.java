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

import com.atypon.wayf.data.device.access.DeviceAccess;
import com.atypon.wayf.data.device.access.DeviceAccessQuery;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;

public interface DeviceAccessDao {
    Single<DeviceAccess> create(DeviceAccess publisherSession);
    Maybe<DeviceAccess> read(Long id);
    Single<DeviceAccess> update(DeviceAccess publisherSession);
    Completable delete(Long id);

    Observable<DeviceAccess> filter(DeviceAccessQuery query);
}