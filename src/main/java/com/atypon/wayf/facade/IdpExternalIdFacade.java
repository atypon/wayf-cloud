package com.atypon.wayf.facade;

import com.atypon.wayf.data.identity.external.IdPExternalId;
import com.atypon.wayf.data.identity.external.IdpExternalIdQuery;
import io.reactivex.Observable;
import io.reactivex.Single;

public interface IdpExternalIdFacade {

    Single<IdPExternalId> create(IdPExternalId externalId);

    Observable<IdPExternalId> filter(IdpExternalIdQuery query);

    Single<IdPExternalId> getOrCreateExternalId(IdPExternalId externalId);
}
