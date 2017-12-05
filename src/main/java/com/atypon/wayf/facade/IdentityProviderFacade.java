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
import com.atypon.wayf.data.identity.IdentityProvider;
import com.atypon.wayf.data.identity.IdentityProviderQuery;
import com.atypon.wayf.data.identity.external.IdPExternalId;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;

import java.util.List;

public interface IdentityProviderFacade {
    Single<IdentityProvider> create(IdentityProvider identityProvider);

    Single<IdentityProvider> read(Long id);

    Completable blockIdentityProviderForLocalId(String localId, Long idpId);

    Completable blockIdentityProviderForGlobalId(String globalId, Long idpId);

    Completable blockIdentityProviderForDevice(Device device, Long idpId);

    Observable<IdentityProvider> filter(IdentityProviderQuery filter);

    Single<IdentityProvider> resolve(IdentityProvider identityProvider);

    Single<IdentityProvider> recordIdentityProviderUse(String localId, IdentityProvider identityProvider);

    Single<IdentityProvider> resolveIdpExternalIds(IdentityProvider identityProvider, List<IdPExternalId> externalIds);
}
