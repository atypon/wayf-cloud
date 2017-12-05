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

import com.atypon.wayf.data.device.Device;
import com.atypon.wayf.data.identity.IdentityProvider;
import com.atypon.wayf.data.identity.IdentityProviderQuery;
import com.atypon.wayf.data.identity.external.IdPExternalId;
import com.atypon.wayf.facade.IdentityProviderFacade;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class IdentityProviderFacadeMockImpl implements IdentityProviderFacade {
    @Override
    public Single<IdentityProvider> create(IdentityProvider identityProvider) {
        return null;
    }

    @Override
    public Single<IdentityProvider> read(Long id) {
        return null;
    }

    @Override
    public Observable<IdentityProvider> filter(IdentityProviderQuery filter) {
        Long[] idpIds = filter.getIds();
        List<IdentityProvider> idps = new LinkedList<>();
        for (Long idpId : idpIds) {
            IdentityProvider idp = new IdentityProvider();
            idp.setId(idpId);
            idps.add(idp);
        }
        return Observable.fromIterable(idps);
    }

    @Override
    public Single<IdentityProvider> resolve(IdentityProvider identityProvider) {
        return null;
    }

    @Override
    public Completable blockIdentityProviderForLocalId(String localId, Long idpId) {
        return null;
    }

    @Override
    public Completable blockIdentityProviderForGlobalId(String globalId, Long idpId) {
        return null;
    }

    @Override
    public Single<IdentityProvider> recordIdentityProviderUse(String localId, IdentityProvider identityProvider) {
        return null;
    }

    @Override
    public Single<IdentityProvider> resolveIdpExternalIds(IdentityProvider identityProvider, List<IdPExternalId> externalIds) {
        return null;
    }

    @Override
    public Completable blockIdentityProviderForDevice(Device device, Long idpId) {
        return null;
    }
}









































































