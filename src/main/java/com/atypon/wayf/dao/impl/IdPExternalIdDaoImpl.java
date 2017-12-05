package com.atypon.wayf.dao.impl;

import com.atypon.wayf.dao.IdPExternalIdDao;
import com.atypon.wayf.data.ServiceException;
import com.atypon.wayf.data.identity.external.IdPExternalId;
import com.atypon.wayf.data.identity.external.IdpExternalIdQuery;
import com.atypon.wayf.database.DbExecutor;
import com.atypon.wayf.reactivex.DaoPolicies;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class IdPExternalIdDaoImpl implements IdPExternalIdDao {

    private static Logger LOG = LoggerFactory.getLogger(com.atypon.wayf.dao.impl.IdPExternalIdDaoImpl.class);

    @Inject
    @Named("external-id.dao.db.create")
    private String createSql;
    @Inject
    @Named("external-id.dao.db.read")
    private String readSql;
    @Inject
    @Named("external-id.dao.db.filter")
    private String filterSql;
    @Inject
    private DbExecutor dbExecutor;

    public IdPExternalIdDaoImpl() {
    }

    @Override
    public Single<IdPExternalId> create(IdPExternalId externalId) {
        return Single.just(externalId)
                .compose(DaoPolicies::applySingle)
                .flatMap((_externalId) -> dbExecutor.executeUpdate(createSql, _externalId))
                .flatMapMaybe(this::read)
                .toSingle();
    }

    @Override
    public Maybe<IdPExternalId> read(Long id) {
        IdpExternalIdQuery query = new IdpExternalIdQuery().setId(id);

        return Single.just(query)
                .compose(DaoPolicies::applySingle)
                .flatMapMaybe((_query) -> dbExecutor.executeSelectFirst(readSql, _query, IdPExternalId.class));
    }

    @Override
    public Observable<IdPExternalId> filter(IdpExternalIdQuery query) {
        return Single.just(query)
                .compose(DaoPolicies::applySingle)
                .flatMapObservable((_query) -> dbExecutor.executeSelect(filterSql, _query, IdPExternalId.class));
    }


}
