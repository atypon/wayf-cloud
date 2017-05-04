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

package com.atypon.wayf.verticle;

import com.atypon.wayf.guice.WayfGuiceModule;
import com.atypon.wayf.reactivex.WayfReactivexConfig;
import com.atypon.wayf.request.ResponseWriter;
import com.atypon.wayf.verticle.routing.*;
import com.google.common.collect.Lists;
import com.google.inject.Guice;
import com.google.inject.Inject;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class WayfVerticle extends AbstractVerticle {
    private static final Logger LOG = LoggerFactory.getLogger(WayfVerticle.class);

    @Inject
    private PublisherSessionRouting publisherSessionRouting;

    @Inject
    private IdentityProviderRouting identityProviderRouting;

    @Inject
    private DeviceRoutingProvider deviceRoutingProvider;

    @Inject
    private PublisherRouting publisherRouting;

    private List<RoutingProvider> routingProviders;

    public WayfVerticle() {
    }

    @Override
    public void start(Future<Void> fut) {
        LOG.info("Starting wayf-cloud server");
        startWebApp((http) -> completeStartup(http, fut));
    }

    private void startWebApp(Handler<AsyncResult<HttpServer>> next) {
        Guice.createInjector(new WayfGuiceModule()).injectMembers(this);
        routingProviders = Lists.newArrayList(publisherSessionRouting, identityProviderRouting, deviceRoutingProvider, publisherRouting);
        // Create a router object.
        Router router = Router.router(vertx);

        LOG.debug("Adding routes");
        routingProviders.forEach((routingProvider) -> routingProvider.addRoutings(router));

        LOG.debug("Adding default error handler to routes");
        for (Route route : router.getRoutes()) {
            route.failureHandler((rc) -> ResponseWriter.buildFailure(rc));
            LOG.debug("Found path {}", route);
        }

        LOG.debug("Starting HTTP server");
        // Create the HTTP server and pass the "accept" method to the request handler.
        vertx
                .createHttpServer()
                .requestHandler(router::accept)
                .listen(
                        // Retrieve the port from the configuration,
                        // default to 8080.
                        config().getInteger("http.port", 8080),
                        next::handle
                );
    }

    private void completeStartup(AsyncResult<HttpServer> http, Future<Void> fut) {
        if (http.succeeded()) {
            initConfigs();

            LOG.info("SUCCESS: wayf-cloud successfully initialized");
            fut.complete();
        } else {
            LOG.debug("FAILURE: Could not start wayf-cloud due to exception", http.cause());
            fut.fail(http.cause());
        }
    }

    private void initConfigs() {
        LOG.info("Initializing server configs");

        WayfReactivexConfig.initializePlugins();
    }
}
