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

import com.atypon.wayf.data.InflationPolicyParser;
import com.atypon.wayf.data.device.Device;
import com.atypon.wayf.data.device.DeviceQuery;
import com.atypon.wayf.data.device.access.DeviceAccess;
import com.atypon.wayf.data.device.access.DeviceAccessQuery;
import com.atypon.wayf.data.device.access.DeviceAccessType;
import com.atypon.wayf.facade.DeviceAccessFacade;
import com.atypon.wayf.facade.DeviceFacade;
import com.atypon.wayf.request.RequestReader;
import com.atypon.wayf.verticle.WayfRequestHandlerFactory;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.reactivex.Observable;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class DeviceAccessRouting implements RoutingProvider {
    private static final Logger LOG = LoggerFactory.getLogger(DeviceAccessRouting.class);

    private static final String GLOBAL_ID_PARAM_NAME = "globalId";

    private static final String MY_DEVICE_ACTIVITY = "/1/mydevice/activity";
    private static final String READ_DEVICE_ACTIVITY = "/1/device/:globalId/activity";

    private static final String GLOBAL_ID_ARG_DESCRIPTION = "Global ID";

    @Inject
    private WayfRequestHandlerFactory handlerFactory;

    @Inject
    private DeviceAccessFacade deviceAccessFacade;

    @Inject
    private DeviceFacade deviceFacade;

    @Inject
    private InflationPolicyParser<String> inflationPolicyParser;

    public DeviceAccessRouting() {
    }

    public void addRoutings(Router router) {
        router.get(MY_DEVICE_ACTIVITY).handler(handlerFactory.observable((rc) -> readMyDeviceLocalHistory(rc)));
    }


    public Observable<DeviceAccess> readMyDeviceLocalHistory(RoutingContext routingContext) {
        LOG.debug("Received create IdentityProvider request");

        String globalId = RequestReader.getCookieValue(routingContext, RequestReader.DEVICE_ID);

        Device device = deviceFacade.read(new DeviceQuery().setGlobalId(globalId)).blockingGet();

        DeviceAccessQuery deviceAccessQuery = new DeviceAccessQuery().setDeviceIds(Lists.newArrayList(device.getId()));

        String type = RequestReader.getQueryValue(routingContext, "type");

        if (type != null) {
            deviceAccessQuery.setType(DeviceAccessType.valueOf(type));
        }

        deviceAccessQuery.setInflationPolicy(inflationPolicyParser.parse(DeviceAccessQuery.PUBLISHER_FIELD + "," + DeviceAccessQuery.IDENTITY_PROVIDER));

        return deviceAccessFacade.filter(deviceAccessQuery);
    }

}
