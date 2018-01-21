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

import com.atypon.wayf.data.device.access.DeviceAccess;
import com.atypon.wayf.guice.WayfGuiceModule;
import com.atypon.wayf.reactivex.WayfReactivexConfig;
import com.atypon.wayf.request.RequestReader;
import com.atypon.wayf.request.ResponseWriter;
import com.atypon.wayf.verticle.routing.*;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.StaticHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class WayfVerticle extends AbstractVerticle {
    private static final Logger LOG = LoggerFactory.getLogger(WayfVerticle.class);

    @Inject
    private IdentityProviderUsageRouting identityProviderUsageRouting;

    @Inject
    private IdentityProviderRouting identityProviderRouting;

    @Inject
    private DeviceRoutingProvider deviceRoutingProvider;

    @Inject
    private PublisherRouting publisherRouting;

    @Inject
    private DeviceAccessRouting deviceAccessRouting;

    @Inject
    private PublisherRegistrationRouting publisherRegistrationRouting;

    @Inject
    private UserRouting userRouting;

    @Inject
    private ResponseWriter responseWriter;

    @Inject
    @Named("wayf.port")
    private Integer wayfPort;

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
        routingProviders = Lists.newArrayList(identityProviderUsageRouting, identityProviderRouting, deviceRoutingProvider, publisherRouting, deviceAccessRouting, publisherRegistrationRouting, userRouting);
        // Create a router object.
        Router router = Router.router(vertx);

        CorsHandler handler = CorsHandler.create("*")
                .allowCredentials(true)
                .allowedMethod(io.vertx.core.http.HttpMethod.PATCH)
                .allowedMethod(io.vertx.core.http.HttpMethod.OPTIONS)
                .allowedMethod(io.vertx.core.http.HttpMethod.DELETE)
                .exposedHeaders(Sets.newHashSet("X-Device-Id"))
                .allowedHeader("Access-Control-Request-Method")
                .allowedHeader("Access-Control-Allow-AuthenticationCredentials")
                .allowedHeader("Access-Control-Allow-Origin")
                .allowedHeader("Access-Control-Allow-Headers")
                .allowedHeader("Content-Type")
                .allowedHeader("Authorization");

        router.route().handler(handler);
        router.route().handler(CookieHandler.create());

        LOG.debug("Adding routes");

        routingProviders.forEach((routingProvider) -> routingProvider.addRoutings(router));

        LOG.debug("Adding default error handler to routes");
        for (Route route : router.getRoutes()) {
            route.failureHandler((rc) -> responseWriter.buildFailure(rc));
            LOG.debug("Found path {}", route);
        }

        router.route("/public/*").handler(StaticHandler.create("public"));

        LOG.debug("Starting HTTP server");
        // Create the HTTP server and pass the "accept" method to the request handler.
        vertx.createHttpServer()
                .requestHandler(router::accept)
                .listen(
                        wayfPort,
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
