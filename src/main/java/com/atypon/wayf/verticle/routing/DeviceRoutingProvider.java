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

import com.atypon.wayf.data.authentication.AuthenticatedEntity;
import com.atypon.wayf.data.authentication.AuthorizationToken;
import com.atypon.wayf.data.InflationPolicyParser;
import com.atypon.wayf.data.ServiceException;
import com.atypon.wayf.data.device.Device;
import com.atypon.wayf.data.device.DeviceQuery;
import com.atypon.wayf.data.publisher.Publisher;
import com.atypon.wayf.facade.ClientJsFacade;
import com.atypon.wayf.facade.DeviceFacade;
import com.atypon.wayf.facade.PublisherFacade;
import com.atypon.wayf.request.RequestContextAccessor;
import com.atypon.wayf.request.RequestParamMapper;
import com.atypon.wayf.request.RequestReader;
import com.atypon.wayf.request.ResponseWriter;
import com.atypon.wayf.verticle.WayfRequestHandlerFactory;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.ext.web.Cookie;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.impl.CookieImpl;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class DeviceRoutingProvider implements RoutingProvider {
    private static final Logger LOG = LoggerFactory.getLogger(DeviceRoutingProvider.class);

    private static final String DEVICE_ID_PARAM_NAME = "id";
    private static final String LOCAL_ID_PARAM = "localId";

    private static final String READ_DEVICE = "/1/device/:id";
    private static final String READ_MY_DEVICE = "/1/mydevice";
    private static final String FILTER_DEVICE = "/1/devices";
    private static final String ADD_DEVICE_PUBLISHER_RELATIONSHIP = "/1/device/:localId";
    private static final String CREATE_GLOBAL_ID = "/1/device/";
    private static final String DELETE_GLOBAL_ID = CREATE_GLOBAL_ID;

    @Inject
    private ResponseWriter responseWriter;

    @Inject
    private DeviceFacade deviceFacade;

    @Inject
    private WayfRequestHandlerFactory handlerFactory;

    @Inject
    private InflationPolicyParser<String> inflationPolicyParser;

    @Inject
    private PublisherFacade publisherFacade;

    @Inject
    @Named("wayf.domain")
    private String wayfDomain;

    @Inject
    @Named("jwtSecret")
    private String jwtSecret;

    public DeviceRoutingProvider() {
    }

    public void addRoutings(Router router) {
        router.route("/1/device*").handler(BodyHandler.create());
        router.get(READ_DEVICE).handler(handlerFactory.single((rc) -> readDevice(rc)));
        router.get(READ_MY_DEVICE).handler(handlerFactory.single((rc) -> readMyDevice(rc)));
        router.get(FILTER_DEVICE).handler(handlerFactory.observable((rc) -> filterDevice(rc)));
        router.post(ADD_DEVICE_PUBLISHER_RELATIONSHIP).handler(handlerFactory.completable((rc) -> registerLocalId(rc)));
        router.patch(ADD_DEVICE_PUBLISHER_RELATIONSHIP).handler(handlerFactory.cookieSingle((rc) -> createPublisherDeviceRelationship(rc)));
        router.post(CREATE_GLOBAL_ID).handler(handlerFactory.cookieSingle(rc -> createGlobalId(rc)));
        router.delete(DELETE_GLOBAL_ID).handler(handlerFactory.completable((rc)-> deleteDevice(rc)));
    }

    public Completable deleteDevice(RoutingContext routingContext){
        Device device = readMyDevice(routingContext).blockingGet();
        return deviceFacade.deleteDevice(device.getId());
    }

    public Single<Device> createGlobalId(RoutingContext rc) {
        LOG.debug("Received create Device request");
        return deviceFacade.create(new Device()).map(device -> addGlobalIdCookie(device, rc));
    }

    public Single<Device> readDevice(RoutingContext routingContext) {
        LOG.debug("Received read Device request");

        DeviceQuery query = buildQuery(routingContext);

        String globalIdParam = RequestReader.readRequiredPathParameter(routingContext, DEVICE_ID_PARAM_NAME, "Global ID");
        query.setGlobalId(globalIdParam);

        return deviceFacade.read(query);
    }

    public Single<Device> readMyDevice(RoutingContext routingContext) {
        LOG.debug("Received read Device request");

        DeviceQuery query = buildQuery(routingContext);

        String deviceId = RequestReader.getCookieValue(routingContext, RequestReader.DEVICE_ID);
        query.setGlobalId(deviceId);

        Observable<Device> device = deviceFacade.filter(query);
        return device.isEmpty().blockingGet() ? createGlobalId(routingContext) : device.singleOrError();

    }

    public Observable<Device> filterDevice(RoutingContext routingContext) {
        LOG.debug("Received read Device request");

        DeviceQuery query = buildQuery(routingContext);

        return deviceFacade.filter(query);
    }

    public Completable registerLocalId(RoutingContext routingContext) {
        String localId = RequestReader.readPathArgument(routingContext, LOCAL_ID_PARAM);

        Publisher publisher = AuthenticatedEntity.authenticatedAsPublisher(RequestContextAccessor.get().getAuthenticated());
        String hashedLocalId = deviceFacade.encryptLocalId(publisher.getId(), localId);

        return deviceFacade.registerLocalId(hashedLocalId);
    }

    public Single<Device> createPublisherDeviceRelationship(RoutingContext routingContext) {
        LOG.debug("Received request to create publisher/device relationship");

        String localId = RequestReader.readPathArgument(routingContext, LOCAL_ID_PARAM);

        AuthorizationToken token = RequestContextAccessor.get().getAuthorizationToken();
        if (token == null) {
            throw new ServiceException(HttpStatus.SC_BAD_REQUEST, "An Authorization token is required");
        }

        LOG.debug("Token value [{}]", token.getValue());

        String publisherCode = null;

        try {
            Algorithm.HMAC256(jwtSecret);
            DecodedJWT jwt = JWT.decode(token.getValue());

            publisherCode = jwt.getClaim(ClientJsFacade.PUBLISHER_CODE_KEY).asString();
        } catch (Exception e) {
            throw new ServiceException(HttpStatus.SC_UNAUTHORIZED, "Could not authenticate token", e);
        }

        LOG.debug("Publisher code {}", publisherCode);

        return publisherFacade.lookupCode(publisherCode)
                .flatMap((publisher) -> {

                    String hashedLocalId = deviceFacade.encryptLocalId(publisher.getId(), localId);

                    return deviceFacade.relateLocalIdToDevice(publisher, hashedLocalId)
                            .map((device) -> addGlobalIdCookie(device, routingContext));

                });
    }

    private Device addGlobalIdCookie(Device device, RoutingContext rc) {
        String globalId = device.getGlobalId();

        Cookie cookie = new CookieImpl(RequestReader.DEVICE_ID, globalId)
                .setMaxAge(158132000l)
                .setPath("/");

        String requestOrigin = RequestReader.getHeaderValue(rc, "Origin");

        LOG.debug("Request origin [{}]", requestOrigin);

        if (requestOrigin != null && !requestOrigin.isEmpty()) {
            rc.response().putHeader("Access-Control-Allow-Origin", requestOrigin);
        }

        rc.addCookie(cookie);
        device.setGlobalId(null);

        return device;
    }

    private DeviceQuery buildQuery(RoutingContext routingContext) {
        DeviceQuery query = new DeviceQuery();

        String fields = RequestReader.getQueryValue(routingContext, "fields");
        if (fields != null) {
            query.setInflationPolicy(inflationPolicyParser.parse(fields));
        }

        RequestParamMapper.mapParams(routingContext, query);

        return query;

    }

}
