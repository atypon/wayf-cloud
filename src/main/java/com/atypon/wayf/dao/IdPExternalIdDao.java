package com.atypon.wayf.dao;

import com.atypon.wayf.data.identity.external.IdPExternalId;
import com.atypon.wayf.data.identity.external.IdpExternalIdQuery;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;

public interface IdPExternalIdDao {

    Single<IdPExternalId> create(IdPExternalId externalId);

    Maybe<IdPExternalId> read(Long id);

    Observable<IdPExternalId> filter(IdpExternalIdQuery query);
}
