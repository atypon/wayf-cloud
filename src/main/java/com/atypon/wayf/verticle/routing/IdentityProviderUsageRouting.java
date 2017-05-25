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

import com.atypon.wayf.data.identity.IdentityProvider;
import com.atypon.wayf.data.identity.IdentityProviderUsage;
import com.atypon.wayf.facade.IdentityProviderFacade;
import com.atypon.wayf.facade.IdentityProviderUsageFacade;
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
public class IdentityProviderUsageRouting implements RoutingProvider {
    private static final Logger LOG = LoggerFactory.getLogger(IdentityProviderUsageRouting.class);

    private static final String LOCAL_ID_PARAM_NAME = "localId";

    private static final String READ_DEVICE_RECENT_HISTORY = "/1/device/:localId/history";

    @Inject
    private IdentityProviderUsageFacade identityProviderUsageFacade;

    @Inject
    private WayfRequestHandlerFactory handlerFactory;

    public IdentityProviderUsageRouting() {
    }

    public void addRoutings(Router router) {
        router.get(READ_DEVICE_RECENT_HISTORY).handler(handlerFactory.observable((rc) -> readDeviceLocalHistory(rc)));
    }

    public Observable<IdentityProviderUsage> readDeviceLocalHistory(RoutingContext routingContext) {
        LOG.debug("Received create IdentityProvider request");

        String localId = RequestReader.readPathArgument(routingContext, LOCAL_ID_PARAM_NAME);

        return identityProviderUsageFacade.buildRecentHistory(localId);
    }
}