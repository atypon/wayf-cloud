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

import com.atypon.wayf.data.publisher.Publisher;
import com.atypon.wayf.data.publisher.PublisherQuery;
import com.atypon.wayf.facade.PublisherFacade;
import com.atypon.wayf.request.RequestParamMapper;
import com.atypon.wayf.request.RequestReader;
import com.atypon.wayf.verticle.WayfRequestHandlerFactory;
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

@Singleton
public class PublisherRouting implements RoutingProvider {
    private static final Logger LOG = LoggerFactory.getLogger(PublisherRouting.class);

    private static final String PUBLISHER_BASE_URL = "/1/publisher";
    private static final String PUBLISHER_ID_PARAM_NAME = "id";
    private static final String PUBLISHER_IDS_PARAM_NAME = "ids";
    private static final String PUBLISHER_ID_PARAM = ":" + PUBLISHER_ID_PARAM_NAME;

    private static final String CREATE_PUBLISHER = PUBLISHER_BASE_URL;
    private static final String READ_PUBLISHER = PUBLISHER_BASE_URL + "/" + PUBLISHER_ID_PARAM;
    private static final String FILTER_PUBLISHERS = PUBLISHER_BASE_URL + "s";
    private static final String DELETE_PUBLISHER = PUBLISHER_BASE_URL + "/" + PUBLISHER_ID_PARAM;
    private static final String PUBLISHER_ID_ARG_DESCRIPTION = "Publisher ID";

    @Inject
    private PublisherFacade publisherFacade;

    @Inject
    private WayfRequestHandlerFactory handlerFactory;

    public PublisherRouting() {
    }

    public void addRoutings(Router router) {
        router.route(PUBLISHER_BASE_URL + "*").handler(BodyHandler.create());
        router.post(CREATE_PUBLISHER).handler(handlerFactory.single((rc) -> createPublisher(rc)));
        router.get(READ_PUBLISHER).handler(handlerFactory.single((rc) -> readPublisher(rc)));
        router.get(FILTER_PUBLISHERS).handler(handlerFactory.observable((rc) -> filterPublishers(rc)));
        router.delete(DELETE_PUBLISHER).handler(handlerFactory.completable((rc) -> removePublisher(rc)));

    }

    public Single<Publisher> createPublisher(RoutingContext routingContext) {
        LOG.debug("Received create PublisherSession request");

        return Single.just(routingContext)
                .flatMap((rc) -> RequestReader.readRequestBody(rc, Publisher.class))
                .flatMap((requestPublisher) -> publisherFacade.create(requestPublisher));
    }

    public Single<Publisher> readPublisher(RoutingContext routingContext) {
        LOG.debug("Received read PublisherSession request");

        Long publisherId = Long.valueOf(RequestReader.readRequiredPathParameter(routingContext, PUBLISHER_ID_PARAM_NAME, PUBLISHER_ID_ARG_DESCRIPTION));

        return publisherFacade.read(publisherId);
    }

    public Observable<Publisher> filterPublishers(RoutingContext routingContext) {
        LOG.debug("Received filter PublisherSession request");

        PublisherQuery publisherQuery = new PublisherQuery();
        RequestParamMapper.mapParams(routingContext, publisherQuery);

        return publisherFacade.filter(publisherQuery);
    }


    public Completable removePublisher(RoutingContext routingContext) {
        LOG.debug("Received Delete PublisherSession request");

        Long publisherId = Long.valueOf(RequestReader.readRequiredPathParameter(routingContext, PUBLISHER_ID_PARAM_NAME, PUBLISHER_ID_ARG_DESCRIPTION));

        return publisherFacade.delete(publisherId);
    }
}
