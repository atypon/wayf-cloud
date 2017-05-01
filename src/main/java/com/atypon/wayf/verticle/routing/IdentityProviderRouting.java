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
import com.atypon.wayf.data.publisher.PublisherSession;
import com.atypon.wayf.facade.IdentityProviderFacade;
import com.atypon.wayf.facade.PublisherSessionFacade;
import com.atypon.wayf.request.RequestReader;
import com.atypon.wayf.verticle.WayfRequestHandler;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import org.apache.commons.collections.map.IdentityMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class IdentityProviderRouting implements RoutingProvider {
    private static final Logger LOG = LoggerFactory.getLogger(IdentityProviderRouting.class);

    private static final String IDENTITY_PROVIDER_BASE_URL = "/1/identityProvider";
    private static final String IDENTITY_PROVIDER_ID_PARAM_NAME = "id";
    private static final String IDENTITY_PROVIDER_ID_PARAM = ":" + IDENTITY_PROVIDER_ID_PARAM_NAME;

    private static final String CREATE_IDENTITY_PROVIDER = IDENTITY_PROVIDER_BASE_URL;
    private static final String READ_IDENTITY_PROVIDER = IDENTITY_PROVIDER_BASE_URL + "/" + IDENTITY_PROVIDER_ID_PARAM;

    @Inject
    private IdentityProviderFacade identityProviderFacade;

    public IdentityProviderRouting() {
    }

    public void addRoutings(Router router) {
        router.route(IDENTITY_PROVIDER_BASE_URL + "*").handler(BodyHandler.create());
        router.post(CREATE_IDENTITY_PROVIDER).handler(WayfRequestHandler.single((rc) -> createIdentityProvider(rc)));
        router.get(READ_IDENTITY_PROVIDER).handler(WayfRequestHandler.single((rc) -> readIdentityProvider(rc)));
    }

    public Single<IdentityProvider> createIdentityProvider(RoutingContext routingContext) {
        LOG.debug("Received create IdentityProvider request");

        return Single.just(routingContext)
                .flatMap((rc) -> RequestReader.readRequestBody(rc, IdentityProvider.class))
                .flatMap((requestIdentityProvider) -> identityProviderFacade.create(requestIdentityProvider));
    }

    public Single<IdentityProvider> readIdentityProvider(RoutingContext routingContext) {
        LOG.debug("Received read IdentityProvider request");

        return Single.just(routingContext)
                .map((rc) -> RequestReader.readPathArgument(rc, IDENTITY_PROVIDER_ID_PARAM_NAME))
                .flatMap((requestIdentityProviderId) -> identityProviderFacade.read(requestIdentityProviderId));
    }
}
