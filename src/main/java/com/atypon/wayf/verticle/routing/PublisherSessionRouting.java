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

import com.atypon.wayf.data.IdentityProvider;
import com.atypon.wayf.data.device.Device;
import com.atypon.wayf.data.publisher.PublisherSession;
import com.atypon.wayf.data.publisher.PublisherSessionFilter;
import com.atypon.wayf.data.publisher.PublisherSessionQuery;
import com.atypon.wayf.facade.PublisherSessionFacade;
import com.atypon.wayf.request.RequestContextAccessor;
import com.atypon.wayf.request.RequestReader;
import com.atypon.wayf.verticle.WayfRequestHandler;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

@Singleton
public class PublisherSessionRouting implements RoutingProvider {
    private static final Logger LOG = LoggerFactory.getLogger(PublisherSessionRouting.class);

    private static final String PUBLISHER_SESSION_BASE_URL = "/1/publisherSession";
    private static final String PUBLISHER_SESSION_ID_PARAM_NAME = "id";
    private static final String PUBLISHER_SESSION_LOCAL_ID_PARAM_NAME = "localId";
    private static final String DEVICE_ID_QUERY_PARAM = "device.id";
    private static final String SESSION_ID_PARAM_NAME = "sessionId";
    private static final String PUBLISHER_SESSION_ID_PARAM = ":" + PUBLISHER_SESSION_ID_PARAM_NAME;
    private static final String PUBLISHER_SESSION_LOCAL_ID_PARAM = ":" + PUBLISHER_SESSION_LOCAL_ID_PARAM_NAME;
    private static final String PUBLISHER_SESSION_PUBLISHER_ID_PARAM = "publisherId=:" + SESSION_ID_PARAM_NAME;

    private static final String CREATE_PUBLISHER_SESSION = PUBLISHER_SESSION_BASE_URL;
    private static final String READ_PUBLISHER_SESSION = PUBLISHER_SESSION_BASE_URL + "/" +  PUBLISHER_SESSION_ID_PARAM;
    private static final String READ_PUBLISHER_SESSION_BY_LOCAL_ID = PUBLISHER_SESSION_BASE_URL + "/" + PUBLISHER_SESSION_LOCAL_ID_PARAM_NAME + "=" +  PUBLISHER_SESSION_LOCAL_ID_PARAM;
    private static final String UPDATE_PUBLISHER_SESSION_BY_PUBLISHER_ID = PUBLISHER_SESSION_BASE_URL + "/" + PUBLISHER_SESSION_PUBLISHER_ID_PARAM;
    private static final String SET_IDP_BY_PUBLISHER_ID = READ_PUBLISHER_SESSION_BY_LOCAL_ID + "/identityProvider";
    private static final String UPDATE_PUBLISHER_SESSION = PUBLISHER_SESSION_BASE_URL + "/" +  PUBLISHER_SESSION_ID_PARAM;
    private static final String DELETE_PUBLISHER_SESSION = PUBLISHER_SESSION_BASE_URL + "/" +  PUBLISHER_SESSION_ID_PARAM;
    private static final String FILTER_PUBLISHER_SESSION = PUBLISHER_SESSION_BASE_URL + "s";

    private PublisherSessionFacade publisherSessionFacade;

    @Inject
    public PublisherSessionRouting(PublisherSessionFacade publisherSessionFacade) {
        this.publisherSessionFacade = publisherSessionFacade;
    }

    public void addRoutings(Router router) {
        router.route(PUBLISHER_SESSION_BASE_URL + "*").handler(BodyHandler.create());
        router.post(CREATE_PUBLISHER_SESSION).handler(WayfRequestHandler.single((rc) -> createPublisherSession(rc)));
        router.get(READ_PUBLISHER_SESSION_BY_LOCAL_ID).handler(WayfRequestHandler.single((rc) -> readPublisherSessionByLocalId(rc)));
        router.get(READ_PUBLISHER_SESSION).handler(WayfRequestHandler.single((rc) -> readPublisherSession(rc)));
        router.put(UPDATE_PUBLISHER_SESSION).handler(WayfRequestHandler.single((rc) -> updatePublisherSession(rc)));
        router.put(SET_IDP_BY_PUBLISHER_ID).handler(WayfRequestHandler.completable((rc) -> addIdp(rc)));
        router.get(FILTER_PUBLISHER_SESSION).handler(WayfRequestHandler.observable((rc) -> filter(rc)));
        router.delete(DELETE_PUBLISHER_SESSION).handler(WayfRequestHandler.completable((rc) -> deletePublisherSession(rc)));
    }

    public Single<PublisherSession> createPublisherSession(RoutingContext routingContext) {
        LOG.debug("Received create PublisherSession request");

        return Single.just(routingContext)
                .flatMap((rc) -> RequestReader.readRequestBody(rc, PublisherSession.class))
                .map((requestPublisherSession) -> {
                        if (RequestContextAccessor.get().getDeviceId() != null) {
                            LOG.debug("Setting request device");

                            Device requestDevice = new Device();
                            requestDevice.setId(RequestContextAccessor.get().getDeviceId());

                            requestPublisherSession.setDevice(requestDevice);
                        }

                        return requestPublisherSession;
                    }
                )
                .flatMap((requestPublisherSession) -> publisherSessionFacade.create(requestPublisherSession));
    }

    public Single<PublisherSession> readPublisherSession(RoutingContext routingContext) {
        LOG.debug("Received read PublisherSession request");

        return Single.just(routingContext)
                .map((rc) -> RequestReader.readPathArgument(rc, PUBLISHER_SESSION_ID_PARAM_NAME))
                .flatMap((id) -> publisherSessionFacade.read(id));
    }

    public Single<PublisherSession> readPublisherSessionByLocalId(RoutingContext routingContext) {
        LOG.debug("Received read PublisherSession by localId request");

        return Single.just(routingContext)
                .map((rc) -> buildFilter(rc))
                .flatMapObservable((query) -> publisherSessionFacade.filter(query))
                .firstOrError();
    }

    public Single<PublisherSession> updatePublisherSession(RoutingContext routingContext) {
        LOG.debug("Received update PublisherSession request");

        return Single.just(routingContext)
                .flatMap((rc) -> RequestReader.readRequestBody(rc, PublisherSession.class))
                .flatMap((requestPublisherSession) -> publisherSessionFacade.update(requestPublisherSession));
    }

    public Completable addIdp(RoutingContext routingContext) {
        LOG.debug("Received update PublisherSession request");

        return Single.just(routingContext)
                .map((_routingContext) -> {
                    String localId = RequestReader.readPathArgument(routingContext, PUBLISHER_SESSION_LOCAL_ID_PARAM_NAME);
                    IdentityProvider identityProvider = RequestReader.readRequestBody(routingContext, IdentityProvider.class).blockingGet();

                    LOG.debug("Publisher local ID[{}] Identity Provider[{}]", localId, identityProvider);

                    PublisherSession publisherSession = new PublisherSession();
                    publisherSession.setLocalId(localId);
                    publisherSession.setAuthenticatedBy(identityProvider);

                    return publisherSession;
                })
                .flatMapCompletable((requestPublisherSession) -> publisherSessionFacade.addIdpRelationship(requestPublisherSession));
    }

    public Observable<PublisherSession> filter(RoutingContext routingContext) {
        LOG.debug("Received filter PublisherSession request");

        return Single.just(routingContext)
                .map((rc) -> buildFilter(rc))
                .flatMapObservable((publisherSessionFilter) -> publisherSessionFacade.filter(publisherSessionFilter));
    }

    public Completable deletePublisherSession(RoutingContext routingContext) {
        LOG.debug("Received delete PublisherSession request");

        return Single.just(routingContext)
                .map((rc) -> RequestReader.readPathArgument(rc, PUBLISHER_SESSION_ID_PARAM_NAME))
                .flatMapCompletable((publisherSessionId) -> publisherSessionFacade.delete(publisherSessionId));
    }

    private PublisherSessionFilter buildFilter(RoutingContext routingContext) {
        String deviceId =  RequestReader.getQueryValue(routingContext, DEVICE_ID_QUERY_PARAM);

        String concatFields = RequestReader.getQueryValue(routingContext, "fields");
        Set<String> fields = null;

        if (concatFields != null) {
            String[] fieldArray = concatFields.split(",");
            fields = Sets.newHashSet(fieldArray);
        }

        String id = RequestReader.readPathArgument(routingContext, PUBLISHER_SESSION_ID_PARAM_NAME);
        String localId = RequestReader.readPathArgument(routingContext, PUBLISHER_SESSION_LOCAL_ID_PARAM_NAME);

        return new PublisherSessionFilter()
                .setDeviceId(deviceId)
                .setLocalId(localId);
    }
}
