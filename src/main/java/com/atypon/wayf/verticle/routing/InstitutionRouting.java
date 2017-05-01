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

package com.atypon.wayf.verticle.routing;

import com.atypon.wayf.data.Institution;
import com.atypon.wayf.facade.InstitutionFacade;
import com.atypon.wayf.facade.impl.InstitutionFacadeImpl;
import com.atypon.wayf.request.RequestReader;
import com.atypon.wayf.verticle.WayfRequestHandler;
import com.google.inject.Inject;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InstitutionRouting implements RoutingProvider {
    private static final Logger LOG = LoggerFactory.getLogger(InstitutionRouting.class);

    private static final String INSTITUTION_BASE_URL = "/1/institution";
    private static final String INSTITUTION_ID_PARAM_NAME = "id";
    private static final String INSTITUTION_ID_PARAM = ":" + INSTITUTION_ID_PARAM_NAME;

    private static final String CREATE_INSTITUTION = INSTITUTION_BASE_URL;
    private static final String READ_INSTITUTION = INSTITUTION_BASE_URL + "/" +  INSTITUTION_ID_PARAM;
    private static final String UPDATE_INSTITUTION = INSTITUTION_BASE_URL + "/" +  INSTITUTION_ID_PARAM;
    private static final String DELETE_INSTITUTION = INSTITUTION_BASE_URL + "/" +  INSTITUTION_ID_PARAM;

    private InstitutionFacade institutionFacade;

    @Inject
    public InstitutionRouting(InstitutionFacade institutionFacade) {
        this.institutionFacade = institutionFacade;
    }

    public void addRoutings(Router router) {
        router.route(INSTITUTION_BASE_URL + "*").handler(BodyHandler.create());
        router.post(CREATE_INSTITUTION).handler(WayfRequestHandler.single((rc) -> createInstitution(rc)));
        router.get(READ_INSTITUTION).handler(WayfRequestHandler.single((rc) -> readInstitution(rc)));
        router.put(UPDATE_INSTITUTION).handler(WayfRequestHandler.single((rc) -> updateInstitution(rc)));
        router.delete(DELETE_INSTITUTION).handler(WayfRequestHandler.completable((rc) -> deleteInstitution(rc)));
    }

    public Single<Institution> createInstitution(RoutingContext routingContext) {
        LOG.debug("Received create institution request");

        return Single.just(routingContext)
                .flatMap((rc) -> RequestReader.readRequestBody(rc, Institution.class))
                .flatMap((requestInstitution) -> institutionFacade.create(requestInstitution));
    }

    public Single<Institution> readInstitution(RoutingContext routingContext) {
        LOG.debug("Received read institution request");

        return Single.just(routingContext)
                .map((rc) -> RequestReader.readPathArgument(rc, INSTITUTION_ID_PARAM_NAME))
                .flatMap((institutionId) -> institutionFacade.read(institutionId));
    }

    public Single<Institution> updateInstitution(RoutingContext routingContext) {
        LOG.debug("Received update institution request");

        return Single.just(routingContext)
                .flatMap((rc) -> RequestReader.readRequestBody(rc, Institution.class))
                .flatMap((requestInstitution) -> institutionFacade.update(requestInstitution));
    }

    public Completable deleteInstitution(RoutingContext routingContext) {
        LOG.debug("Received delete institution request");

        return Single.just(routingContext)
                .map((rc) -> RequestReader.readPathArgument(rc, INSTITUTION_ID_PARAM_NAME))
                .flatMapCompletable((institutionId) -> institutionFacade.delete(institutionId));
    }
}
