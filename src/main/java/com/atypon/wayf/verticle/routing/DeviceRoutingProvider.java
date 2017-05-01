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

import com.atypon.wayf.data.device.Device;
import com.atypon.wayf.facade.DeviceFacade;
import com.atypon.wayf.request.RequestReader;
import com.atypon.wayf.verticle.WayfRequestHandler;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.reactivex.Single;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class DeviceRoutingProvider implements RoutingProvider {
    private static final Logger LOG = LoggerFactory.getLogger(DeviceRoutingProvider.class);

    private static final String DEVICE_BASE_URL = "/1/device";
    private static final String DEVICE_ID_PARAM_NAME = "id";
    private static final String DEVICE_ID_PARAM = ":" + DEVICE_ID_PARAM_NAME;

    private static final String CREATE_DEVICE = DEVICE_BASE_URL;
    private static final String READ_DEVICE = DEVICE_BASE_URL + "/" +  DEVICE_ID_PARAM;
    private static final String UPDATE_DEVICE = DEVICE_BASE_URL + "/" +  DEVICE_ID_PARAM;
    private static final String DELETE_DEVICE = DEVICE_BASE_URL + "/" +  DEVICE_ID_PARAM;

    private DeviceFacade deviceFacade;

    @Inject
    public DeviceRoutingProvider(DeviceFacade deviceFacade) {
        this.deviceFacade = deviceFacade;
    }

    public void addRoutings(Router router) {
        router.route(DEVICE_BASE_URL + "*").handler(BodyHandler.create());
        router.post(CREATE_DEVICE).handler(WayfRequestHandler.single((rc) -> createDevice(rc)));
        router.get(READ_DEVICE).handler(WayfRequestHandler.single((rc) -> readDevice(rc)));
    }

    public Single<Device> createDevice(RoutingContext routingContext) {
        LOG.debug("Received create Device request");

        return Single.just(routingContext)
                .flatMap((rc) -> RequestReader.readRequestBody(rc, Device.class))
                .flatMap((requestDevice) -> deviceFacade.create(requestDevice));
    }

    public Single<Device> readDevice(RoutingContext routingContext) {
        LOG.debug("Received read Device request");

        return Single.just(routingContext)
                .map((rc) -> RequestReader.readPathArgument(rc, DEVICE_ID_PARAM_NAME))
                .flatMap((deviceId) -> deviceFacade.read(deviceId));
    }
}
