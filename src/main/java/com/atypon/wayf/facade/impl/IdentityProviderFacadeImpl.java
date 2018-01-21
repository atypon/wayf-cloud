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

import com.atypon.wayf.dao.IdentityProviderDao;
import com.atypon.wayf.data.authentication.AuthenticatedEntity;
import com.atypon.wayf.data.ServiceException;
import com.atypon.wayf.data.device.Device;
import com.atypon.wayf.data.device.DeviceQuery;
import com.atypon.wayf.data.device.access.DeviceAccess;
import com.atypon.wayf.data.device.access.DeviceAccessType;
import com.atypon.wayf.data.identity.*;
import com.atypon.wayf.data.identity.external.IdPExternalId;
import com.atypon.wayf.data.identity.external.IdpExternalIdQuery;
import com.atypon.wayf.data.publisher.Publisher;
import com.atypon.wayf.facade.*;
import com.atypon.wayf.request.RequestContextAccessor;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import io.reactivex.Completable;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static com.atypon.wayf.reactivex.FacadePolicies.singleOrException;

@Singleton
public class IdentityProviderFacadeImpl implements IdentityProviderFacade {
    private static final Logger LOG = LoggerFactory.getLogger(IdentityProviderFacadeImpl.class);

    @Inject
    @Named("identityProviderDaoMap")
    private Map<IdentityProviderType, IdentityProviderDao> daosByType;

    @Inject
    private DeviceFacade deviceFacade;

    @Inject
    private DeviceAccessFacade deviceAccessFacade;

    @Inject
    private IdpExternalIdFacade externalIdFacade;

    @Inject
    private DeviceIdentityProviderBlacklistFacade blacklistFacade;

    public IdentityProviderFacadeImpl() {
    }

    @Override
    public Single<IdentityProvider> create(IdentityProvider identityProvider) {
        IdentityProviderDao dao = daosByType.get(identityProvider.getType());

        if (dao == null) {
            throw new ServiceException(HttpStatus.SC_BAD_REQUEST, "IdentityProvider of type [" + identityProvider.getType() + "] not supported");
        }

        return dao.create(identityProvider);
    }

    @Override
    public Single<IdentityProvider> resolveIdpExternalIds(IdentityProvider identityProvider, List<IdPExternalId> externalIds) {
        //update the external id's only if the identity provider doesn't have ones before
        if ((identityProvider.getExternalIds() == null || identityProvider.getExternalIds().isEmpty()) && externalIds != null && !externalIds.isEmpty()) {
            List<IdPExternalId> persistedExtIds = new LinkedList<>();
            Observable.fromIterable(externalIds).
                    subscribe((externalId) -> {
                        externalId.setIdentityProvider(identityProvider);
                        externalIdFacade.getOrCreateExternalId(externalId).doOnError(e -> LOG.error(e.getLocalizedMessage())).subscribe((Consumer<IdPExternalId>) persistedExtIds::add);
                    });
            persistedExtIds.sort(Comparator.comparing(IdPExternalId::getProvider));
            identityProvider.setExternalIds(persistedExtIds.isEmpty() ? null : persistedExtIds);
        }
        return Single.just(identityProvider);
    }

    @Override
    public Single<IdentityProvider> read(Long id) {
        Collection<IdentityProviderDao> daos = daosByType.values();

        // Loop through the DAOs and try to read the ID from each. Return the first entry found
        return singleOrException(Observable.fromIterable(daos)
                .flatMapMaybe((dao) -> dao.read(id)), HttpStatus.SC_BAD_REQUEST, "Invalid IdentityProvider ID").
                flatMap(identityProvider -> externalIdFacade.
                        filter(new IdpExternalIdQuery().setIdpId(identityProvider.getId())).
                        collectInto(new LinkedList<IdPExternalId>(), LinkedList::add).
                        map(list -> {
                            identityProvider.setExternalIds(list.isEmpty() ? null : list);
                            return identityProvider;
                        })
                );
    }

    @Override
    public Single<IdentityProvider> resolve(IdentityProvider identityProvider) {
        LOG.debug("Attempting to resolve IdentityProvider [{}]", identityProvider);

        if (identityProvider.getType() == null) {
            throw new ServiceException(HttpStatus.SC_BAD_REQUEST, "In order to resolve an IdentityProvider, 'type' is required");
        }

        Observable<IdentityProvider> foundProviders = null;

        if (identityProvider.getType() == IdentityProviderType.OAUTH) {
            foundProviders = resolveOauth((OauthEntity) identityProvider);
        } else if (identityProvider.getType() == IdentityProviderType.OPEN_ATHENS) {
            foundProviders = resolveOpenAthens((OpenAthensEntity) identityProvider);
        } else if (identityProvider.getType() == IdentityProviderType.SAML) {
            foundProviders = resolveSamlEntity((SamlEntity) identityProvider);
        } else {
            throw new ServiceException(HttpStatus.SC_BAD_REQUEST, "IdentityProvider of type [" + identityProvider.getType() + "] not supported");
        }


        return singleOrException(
                foundProviders.switchIfEmpty(create(identityProvider).flatMap(idp -> resolveIdpExternalIds(idp, identityProvider.getExternalIds())).toObservable()),
                HttpStatus.SC_BAD_REQUEST,
                "Could not determine a unique IdentityProvider from input");
    }

    @Override
    public Single<IdentityProvider> recordIdentityProviderUse(String localId, IdentityProvider identityProvider) {
        return Single.zip(
                // Resolve the persisted IdentityProvider from the input
                resolve(identityProvider),

                // Read the device by the local ID passed in and authenticated publisher
                deviceFacade.readByLocalId(localId),

                // Once the device and IdentityProvider have create a DeviceAccess object to contain both items
                (resolvedIdentityProvider, device) -> new DeviceAccess.Builder()
                        .device(device)
                        .publisher(AuthenticatedEntity.authenticatedAsPublisher(RequestContextAccessor.get().getAuthenticated()))
                        .identityProvider(resolvedIdentityProvider)
                        .type(DeviceAccessType.ADD_IDP)
                        .build()
        ).flatMap((deviceAccess) ->
                // Run some tasks and return the IDP when they complete
                Completable.mergeArray(
                        // Remove the IDP from the blacklist if it was on it
                        blacklistFacade.remove(deviceAccess.getDevice(), deviceAccess.getIdentityProvider()).subscribeOn(Schedulers.io()),

                        // Log the device access
                        deviceAccessFacade.create(deviceAccess).toCompletable().subscribeOn(Schedulers.io())

                ).toSingleDefault(deviceAccess.getIdentityProvider())
        );
    }

    @Override
    public Completable blockIdentityProviderForLocalId(String localId, Long idpId) {
        return deviceFacade.readByLocalId(localId).flatMapCompletable((device) -> blockIdentityProviderForDevice(device, idpId));
    }

    @Override
    public Completable blockIdentityProviderForGlobalId(String globalId, Long idpId) {
        return deviceFacade.read(new DeviceQuery().setGlobalId(globalId)).flatMapCompletable((device) -> blockIdentityProviderForDevice(device, idpId));
    }

    @Override
    public Completable blockIdentityProviderForDevice(Device device, Long idpId) {
        final Publisher publisher = RequestContextAccessor.get().getAuthenticated() != null ?
                AuthenticatedEntity.authenticatedAsPublisher(RequestContextAccessor.get().getAuthenticated()) : null;

        return read(idpId)
                .map((identityProvider) ->
                        new DeviceAccess.Builder()
                                .device(device)
                                .publisher(publisher)
                                .identityProvider(identityProvider)
                                .type(DeviceAccessType.REMOVE_IDP)
                                .build()

                ).flatMapCompletable((deviceAccess) ->
                        Completable.mergeArray(
                                blacklistFacade.add(deviceAccess.getDevice(), deviceAccess.getIdentityProvider()).subscribeOn(Schedulers.io()),
                                deviceAccessFacade.create(deviceAccess).toCompletable().subscribeOn(Schedulers.io())
                        )
                );
    }

    @Override
    public Observable<IdentityProvider> filter(IdentityProviderQuery query) {
        // If a type is known, only query that DAO. If no type is known, concat the results of filtering all DAOs
        Observable<IdentityProvider> result;
        if (query.getType() != null) {
            result = daosByType.get(query.getType()).filter(query);
        } else {
            Collection<IdentityProviderDao> daos = daosByType.values();

            result = Observable.fromIterable(daos)
                    .flatMap((dao) -> dao.filter(query))
                    .collectInto(new LinkedList<IdentityProvider>(), LinkedList::add)
                    .flatMapObservable(Observable::fromIterable);
        }
        return result.flatMap(idp -> externalIdFacade.filter(new IdpExternalIdQuery().setIdpId(idp.getId())).
                collectInto(new LinkedList<IdPExternalId>(), LinkedList::add).
                map(list -> {
                    idp.setExternalIds(list.isEmpty() ? null : list);
                    return idp;
                }).toObservable()).sorted(Comparator.comparing(IdentityProvider::getId));
    }

    private Observable<IdentityProvider> resolveOauth(OauthEntity oauthEntity) {
        if (oauthEntity.getProvider() == null) {
            throw new ServiceException(HttpStatus.SC_BAD_REQUEST, "In order to resolve an OAUTH Entity, 'provider' is required");
        }

        IdentityProviderQuery query = new IdentityProviderQuery()
                .setType(IdentityProviderType.OAUTH)
                .setProvider(oauthEntity.getProvider());

        return filter(query);
    }

    private Observable<IdentityProvider> resolveOpenAthens(OpenAthensEntity openAthensEntity) {
        if (openAthensEntity.getOrganizationId() == null) {
            throw new ServiceException(HttpStatus.SC_BAD_REQUEST, "In order to resolve an OpenAthens Entity, 'organizationId' is required");
        }

        IdentityProviderQuery query = new IdentityProviderQuery()
                .setType(IdentityProviderType.OPEN_ATHENS)
                .setOrganizationId(openAthensEntity.getOrganizationId());

        return filter(query);
    }

    private Observable<IdentityProvider> resolveSamlEntity(SamlEntity samlEntity) {
        if (samlEntity.getEntityId() == null) {
            throw new ServiceException(HttpStatus.SC_BAD_REQUEST, "In order to resolve a SAML Entity, 'entityId' is required");
        }

        IdentityProviderQuery query = new IdentityProviderQuery()
                .setType(IdentityProviderType.SAML)
                .setEntityId(samlEntity.getEntityId());

        return filter(query);
    }
}
